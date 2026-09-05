package dev.crazo7924.onlymusic.features.search

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.schabi.newpipe.extractor.InfoItem

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private val musicRepository: MusicRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { musicRepository.getRecentSongs() } returns flowOf(emptyList())
        coEvery { musicRepository.getRecentQueries() } returns flowOf(listOf("rock", "jazz"))
        coEvery { musicRepository.getSearchSuggestions(any()) } returns Result.success(emptyList())
        viewModel = SearchViewModel(musicRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads recent queries into state`() = runTest {
        advanceUntilIdle()
        assertEquals(listOf("rock", "jazz"), viewModel.uiState.value.recentQueries)
    }

    @Test
    fun `updateQueryFrom fetches search suggestions`() = runTest {
        val query = "hello"
        val expectedSuggestions = listOf("hello world", "hello darkness")
        coEvery { musicRepository.getSearchSuggestions(query) } returns Result.success(expectedSuggestions)

        viewModel.updateQueryFrom(query)
        advanceUntilIdle()

        assertEquals("hello", viewModel.uiState.value.query)
        assertEquals(expectedSuggestions, viewModel.uiState.value.querySuggestions)
    }

    @Test
    fun `search adds query to recent queries history`() = runTest {
        val testQuery = "test"
        val mockItem = MediaListItem(id = "1", title = "Test Song", artist = "Test Artist", infoType = InfoItem.InfoType.STREAM, thumbnailUri = "dummy", duration = 1000L)
        coEvery { musicRepository.search(testQuery) } returns flowOf(Result.success(mockItem))

        viewModel.updateQueryFrom(testQuery)
        viewModel.search()
        advanceUntilIdle()

        coVerify { musicRepository.addRecentQuery(testQuery) }
    }

    @Test
    fun `deleteRecentQuery triggers repository deletion`() = runTest {
        viewModel.deleteRecentQuery("rock")
        advanceUntilIdle()

        coVerify { musicRepository.deleteRecentQuery("rock") }
    }

    @Test
    fun `search with short query resets state`() {
        viewModel.updateQueryFrom("a")
        viewModel.search()

        assertEquals(SearchState.INITIAL, viewModel.uiState.value.searchState)
        assertEquals(emptyList<MediaListItem>(), viewModel.uiState.value.suggestions)
        coVerify(exactly = 0) { musicRepository.search(any()) }
    }

    @Test
    fun `search with valid query updates state to SUCCESS`() = runTest {
        val testQuery = "test"
        val mockItem = MediaListItem(id = "1", title = "Test Song", artist = "Test Artist", infoType = InfoItem.InfoType.STREAM, thumbnailUri = "dummy", duration = 1000L)
        val mockFlow = flowOf(Result.success(mockItem))

        coEvery { musicRepository.search(testQuery) } returns mockFlow

        viewModel.updateQueryFrom(testQuery)
        viewModel.search()

        assertEquals(SearchState.SEARCHING, viewModel.uiState.value.searchState)

        advanceUntilIdle() // let the coroutine finish

        assertEquals(SearchState.SUCCESS, viewModel.uiState.value.searchState)
        assertEquals(listOf(mockItem), viewModel.uiState.value.suggestions)
    }

    @Test
    fun `search with empty results updates state to ERROR`() = runTest {
        val testQuery = "empty"
        // Return an empty flow (no elements collected)
        coEvery { musicRepository.search(testQuery) } returns flowOf()

        viewModel.updateQueryFrom(testQuery)
        viewModel.search()

        advanceUntilIdle()

        assertEquals(SearchState.ERROR, viewModel.uiState.value.searchState)
    }
}
