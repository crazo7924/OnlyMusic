/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import android.util.Log
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.Artist
import dev.crazo7924.onlymusic.data.db.ArtistDao
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistSongsCrossRef
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongArtistCrossRef
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.di.RemoteRepository
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URI
import javax.inject.Inject

class CachingMusicRepository @Inject constructor(
    @param:RemoteRepository private val remoteRepository: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
    private val artistDao: ArtistDao,
) : MusicRepository by remoteRepository {

    private suspend fun saveToRecents(mediaListItem: MediaListItem) {
        val recentPlaylistId = playlistDao.getRecentPlaylistId()
        val song = Song(
            songId = mediaListItem.id,
            title = mediaListItem.title ?: "",
            uri = mediaListItem.mediaUri?.let { URI.create(it) } ?: URI(""),
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

    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> {
        val result = remoteRepository.loadMediaUri(uri)
        result.getOrNull()?.let { saveToRecents(it) }
        return result
    }

    override suspend fun loadPlaylistUri(uri: String?): Flow<Result<MediaListItem>> = flow {
        remoteRepository.loadPlaylistUri(uri).collect { result ->
            result.getOrNull()?.let { saveToRecents(it) }
            emit(result)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun loadAutoPlaylistUri(uri: String?): Flow<Result<MediaListItem>> = flow {
        remoteRepository.loadAutoPlaylistUri(uri).collect { result ->
            result.getOrNull()?.let { saveToRecents(it) }
            emit(result)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun loadMorePlaylistItems(): Flow<Result<MediaListItem>> = flow {
        remoteRepository.loadMorePlaylistItems().collect { result ->
            result.getOrNull()?.let { saveToRecents(it) }
            emit(result)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun search(query: String): Flow<Result<MediaListItem>> {
        return remoteRepository.search(query)
    }

    override suspend fun getRecentSongs(): Flow<List<MediaListItem>> {
        Log.d(TAG, "getRecentSongs: about to fetch")
        return flow {
            val songs =
                playlistDao.getRecentSongs()?.songs?.map { it.toMediaListItem() } ?: emptyList()
            Log.d(TAG, "getRecentSongs: ${songs.size} found")
            emit(songs)
        }.flowOn(Dispatchers.IO)
    }

    companion object {
        private const val TAG: String = "CachingMusicRepository"
    }
}
