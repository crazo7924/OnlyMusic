package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.Playlist
import dev.crazo7924.onlymusic.data.db.PlaylistType
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistWithSongs
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongWithArtists
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
import java.net.URI
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CachingMusicRepositoryTest {

    private lateinit var repository: CachingMusicRepository
    private val remoteRepository: MusicRepository = mockk()
    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private val songDao: SongDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = CachingMusicRepository(remoteRepository, playlistDao, songDao)
    }

    @Test
    fun `search caches results from remote and appends local recent songs`() = runTest {
        val query = "test query"
        val remoteItem = MediaListItem(id = "1", title = "Remote Song", artist = "Artist", infoType = InfoItem.InfoType.STREAM, duration = 1000L, thumbnailUri = "http://thumb", mediaUri = "http://media")

        // Mock remote search returning one item
        coEvery { remoteRepository.search(query) } returns flowOf(Result.success(remoteItem))

        val recentPlaylistId = UUID.randomUUID().toString()
        every { playlistDao.getRecentPlaylistId() } returns recentPlaylistId

        // Mock local recent songs
        val localSong = Song(songId = "2", title = "Local Song", duration = 2000L, uri = URI("http://local"), artworkUri = null)
        val songWithArtists = SongWithArtists(song = localSong, artists = emptyList())
        val playlistWithSongs = PlaylistWithSongs(
            playlist = Playlist(playlistId = UUID.randomUUID(), name = "recent", uri = null, playlistType = PlaylistType.INTERNAL),
            songs = listOf(songWithArtists)
        )
        every { playlistDao.getRecentSongs() } returns playlistWithSongs

        // Execute search and collect results
        val results = repository.search(query).toList()

        // Verify we got the remote item and then the local item
        assertEquals(2, results.size)
        assertEquals(remoteItem, results[0].getOrNull())
        assertEquals("2", results[1].getOrNull()?.id)
        assertEquals("Local Song", results[1].getOrNull()?.title)

        // Verify caching operations happened
        coVerify { songDao.insertSong(any()) }
        coVerify { playlistDao.insertSongToPlaylist(any()) }
    }
}
