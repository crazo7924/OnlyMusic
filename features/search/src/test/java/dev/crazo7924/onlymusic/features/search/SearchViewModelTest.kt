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
    private val musicRepository: MusicRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(musicRepository, minQueryLength = 2)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateQueryFrom updates query state correctly`() {
        viewModel.updateQueryFrom("hello")
        assertEquals("hello", viewModel.uiState.value.query)
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
