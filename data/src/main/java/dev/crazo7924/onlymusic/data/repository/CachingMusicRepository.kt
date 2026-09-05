/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import android.util.Log
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.Artist
import dev.crazo7924.onlymusic.data.db.ArtistDao
import dev.crazo7924.onlymusic.data.db.Playlist
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistSongsCrossRef
import dev.crazo7924.onlymusic.data.db.PlaylistType
import dev.crazo7924.onlymusic.data.db.SearchHistoryDao
import dev.crazo7924.onlymusic.data.db.SearchHistoryEntity
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongArtistCrossRef
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.di.RemoteRepository
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.UUID
import javax.inject.Inject

class CachingMusicRepository @Inject constructor(
    @param:RemoteRepository private val remoteRepository: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
    private val artistDao: ArtistDao,
    private val searchHistoryDao: SearchHistoryDao,
) : MusicRepository by remoteRepository {

    override suspend fun saveToRecents(mediaListItem: MediaListItem) = withContext(Dispatchers.IO) {
        var recentPlaylistId = playlistDao.getRecentPlaylistId()

        if (recentPlaylistId == null) {
            val newPlaylistId = UUID.randomUUID()
            playlistDao.insertPlaylist(
                Playlist(
                    playlistId = newPlaylistId,
                    name = "recent",
                    uri = null,
                    playlistType = PlaylistType.INTERNAL
                )
            )
            recentPlaylistId = newPlaylistId.toString()
        }

        val song = Song(
            songId = mediaListItem.id,
            title = mediaListItem.title ?: "",
            uri = mediaListItem.mediaUri?.let { URI.create("https://music.youtube.com/watch?v=${mediaListItem.id}") } ?: URI(""),
            artworkUri = mediaListItem.thumbnailUri?.let { URI.create(it) },
            duration = mediaListItem.duration ?: 0L
        )
        songDao.insertSong(song)

        mediaListItem.artist?.let { artistName ->
            var artist = artistDao.getArtistByName(artistName)
            if (artist == null) {
                artist = Artist(name = artistName)
                artistDao.insertArtist(artist)
            }
            artistDao.insertSongArtistCrossRef(
                SongArtistCrossRef(
                    songId = song.songId,
                    artistId = artist.artistId
                )
            )
        }

        playlistDao.insertSongToPlaylist(
            PlaylistSongsCrossRef(
                playlistId = recentPlaylistId, songId = song.songId
            )
        )
    }

    override suspend fun search(query: String): Flow<Result<MediaListItem>> {
        return remoteRepository.search(query)
    }


    override suspend fun getRecentSongs(): Flow<List<MediaListItem>> {
        Log.d(TAG, "getRecentSongs: about to fetch")
        return flow {
            val songs =
                playlistDao.getRecentSongs()?.songs?.map { it.toMediaListItem() } ?: emptyList()
            Log.d(TAG, "getRecentSongs: ${songs.size} found")
            emit(songs) // Correctly emit the fetched songs
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getRecentQueries(): Flow<List<String>> {
        return searchHistoryDao.getRecentQueries()
    }

    override suspend fun addRecentQuery(query: String) {
        if (query.isNotBlank()) {
            withContext(Dispatchers.IO) {
                searchHistoryDao.insertOrUpdateQuery(
                    SearchHistoryEntity(
                        query = query.trim(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun deleteRecentQuery(query: String) {
        withContext(Dispatchers.IO) {
            searchHistoryDao.deleteQuery(query)
        }
    }

    companion object {
        private const val TAG: String = "CachingMusicRepository"
    }
}
