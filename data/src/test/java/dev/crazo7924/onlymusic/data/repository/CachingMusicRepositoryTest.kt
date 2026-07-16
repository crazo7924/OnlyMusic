package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.Playlist
import dev.crazo7924.onlymusic.data.db.PlaylistType
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistWithSongs
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongWithArtists
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.db.ArtistDao
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
import java.net.URI
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CachingMusicRepositoryTest {

    private lateinit var repository: CachingMusicRepository
    private val remoteRepository: MusicRepository = mockk()
    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private val songDao: SongDao = mockk(relaxed = true)
    private val artistDao: ArtistDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = CachingMusicRepository(remoteRepository, playlistDao, songDao, artistDao)
    }

    @Test
    fun `search caches results from remote without appending local recent songs`() = runTest {
        val query = "test query"
        val remoteItem = MediaListItem(id = "1", title = "Remote Song", artist = "Artist", infoType = InfoItem.InfoType.STREAM, duration = 1000L, thumbnailUri = "http://thumb", mediaUri = "http://media")

        // Mock remote search returning one item
        coEvery { remoteRepository.search(query) } returns flowOf(Result.success(remoteItem))

        val recentPlaylistId = UUID.randomUUID().toString()
        every { playlistDao.getRecentPlaylistId() } returns recentPlaylistId

        // Execute search and collect results
        val results = repository.search(query).toList()

        // Verify we got the remote item
        assertEquals(1, results.size)
        assertEquals(remoteItem, results[0].getOrNull())

        // Verify caching operations happened
        coVerify { songDao.insertSong(any()) }
        coVerify { playlistDao.insertSongToPlaylist(any()) }
    }
}
