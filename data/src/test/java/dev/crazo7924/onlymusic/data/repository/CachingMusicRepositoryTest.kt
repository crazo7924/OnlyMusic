/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.ArtistDao
import dev.crazo7924.onlymusic.data.db.PlaylistDao
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

    @Before
    fun setup() {
        repository = CachingMusicRepository(remoteRepository, playlistDao, songDao, artistDao)
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
    fun `loadMediaUri saves item to recents on success`() = runTest {
        val uri = "http://media"
        val mediaItem = MediaListItem(
            id = "1",
            title = "Test Song",
            artist = "Artist",
            infoType = InfoItem.InfoType.STREAM,
            duration = 1000L,
            thumbnailUri = "http://thumb",
            mediaUri = uri
        )
        val recentPlaylistId = UUID.randomUUID().toString()

        every { playlistDao.getRecentPlaylistId() } returns recentPlaylistId
        coEvery { remoteRepository.loadMediaUri(uri) } returns Result.success(mediaItem)

        val result = repository.loadMediaUri(uri)

        assertEquals(mediaItem, result.getOrNull())
        coVerify { songDao.insertSong(any()) }
        coVerify { playlistDao.insertSongToPlaylist(any()) }
    }

    @Test
    fun `loadPlaylistUri saves items to recents on success`() = runTest {
        val uri = "http://playlist"
        val mediaItem = MediaListItem(
            id = "1",
            title = "Test Song",
            artist = "Artist",
            infoType = InfoItem.InfoType.STREAM,
            duration = 1000L,
            thumbnailUri = "http://thumb",
            mediaUri = "http://media"
        )
        val recentPlaylistId = UUID.randomUUID().toString()

        every { playlistDao.getRecentPlaylistId() } returns recentPlaylistId
        coEvery { remoteRepository.loadPlaylistUri(uri) } returns flowOf(Result.success(mediaItem))

        val results = repository.loadPlaylistUri(uri).toList()

        assertEquals(1, results.size)
        assertEquals(mediaItem, results[0].getOrNull())
        coVerify { songDao.insertSong(any()) }
        coVerify { playlistDao.insertSongToPlaylist(any()) }
    }
}
