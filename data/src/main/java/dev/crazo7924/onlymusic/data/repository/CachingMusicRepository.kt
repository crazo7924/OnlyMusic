/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistSongsCrossRef
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.net.URI

class CachingMusicRepository(
    private val remoteRepository: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
) : MusicRepository by remoteRepository {

    override suspend fun search(query: String): Flow<Result<MediaListItem>> = flow {

        val remoteResults = remoteRepository.search(query)

        remoteResults.collect { result ->
            emit(result)
            result.getOrNull()?.let { mediaListItem ->
                val song = Song(
                    songId = mediaListItem.id,
                    title = mediaListItem.title ?: "",
                    uri = mediaListItem.mediaUri?.let { URI.create(it) } ?: URI(""),
                    artworkUri = mediaListItem.thumbnailUri?.let { URI.create(it) },
                    duration = mediaListItem.duration ?: 0L
                )
                songDao.insertSong(song)

                val recentPlaylistId = playlistDao.getRecentPlaylistId()

                playlistDao.insertSongToPlaylist(
                    PlaylistSongsCrossRef(
                        playlistId = recentPlaylistId, songId = song.songId
                    )
                )
            }
        }

    }.flowOn(Dispatchers.IO)

    override fun getRecentSongs(): Flow<List<MediaListItem>> {
        return playlistDao.getRecentSongsFlow()
            .map { playlist ->
                playlist?.songs?.map { it.toMediaListItem() } ?: emptyList()
            }
    }
}