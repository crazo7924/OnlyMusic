package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.ArtistDao
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.SearchHistoryDao
import dev.crazo7924.onlymusic.data.db.SearchHistoryEntity
import dev.crazo7924.onlymusic.data.db.SongDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.schabi.newpipe.extractor.InfoItem
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CachingMusicRepositoryTest {

    private lateinit var repository: CachingMusicRepository
    private val remoteRepository: MusicRepository = mockk()
    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private val songDao: SongDao = mockk(relaxed = true)
    private val artistDao: ArtistDao = mockk(relaxed = true)
    private val searchHistoryDao: SearchHistoryDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = CachingMusicRepository(
            remoteRepository,
            playlistDao,
            songDao,
            artistDao,
            searchHistoryDao
        )
    }

    @Test
    fun `search returns results from remote without saving to recents`() = runTest {
        val query = "test query"
        val remoteItem = MediaListItem(
            id = "1",
            title = "Remote Song",
            artist = "Artist",
            infoType = InfoItem.InfoType.STREAM,
            duration = 1000L,
            thumbnailUri = "http://thumb",
            mediaUri = "http://media"
        )

        coEvery { remoteRepository.search(query) } returns flowOf(Result.success(remoteItem))

        val results = repository.search(query).toList()

        assertEquals(1, results.size)
        assertEquals(remoteItem, results[0].getOrNull())

        coVerify(exactly = 0) { songDao.insertSong(any()) }
        coVerify(exactly = 0) { playlistDao.insertSongToPlaylist(any()) }
    }

    @Test
    fun `saveToRecents saves item correctly and handles missing playlist`() = runTest {
        val mediaItem = MediaListItem(
            id = "1",
            title = "Test Song",
            artist = "Artist",
            infoType = InfoItem.InfoType.STREAM,
            duration = 1000L,
            thumbnailUri = "http://thumb",
            mediaUri = "http://media"
        )
        
        // Mock missing playlist first, then successful retrieval
        every { playlistDao.getRecentPlaylistId() } returns null andThen "new-playlist-id"

        repository.saveToRecents(mediaItem)

        coVerify { playlistDao.insertPlaylist(any()) }
        coVerify { songDao.insertSong(any()) }
        coVerify { playlistDao.insertSongToPlaylist(any()) }
    }

    @Test
    fun `getRecentQueries delegates to searchHistoryDao`() = runTest {
        val expectedQueries = listOf("rock", "pop")
        coEvery { searchHistoryDao.getRecentQueries() } returns flowOf(expectedQueries)

        val result = repository.getRecentQueries().toList()

        assertEquals(listOf(expectedQueries), result)
    }

    @Test
    fun `addRecentQuery inserts query into searchHistoryDao`() = runTest {
        val query = " rock music "
        repository.addRecentQuery(query)

        coVerify {
            searchHistoryDao.insertOrUpdateQuery(
                match { it.query == "rock music" }
            )
        }
    }

    @Test
    fun `deleteRecentQuery deletes query from searchHistoryDao`() = runTest {
        val query = "rock music"
        repository.deleteRecentQuery(query)

        coVerify { searchHistoryDao.deleteQuery("rock music") }
    }
}
