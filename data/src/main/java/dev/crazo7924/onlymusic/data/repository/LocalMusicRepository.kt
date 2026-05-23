/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalMusicRepository(private val playlistDao: PlaylistDao) : MusicRepository {
    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> {
        return Result.failure(
            Exception("App is in offline mode")
        )
    }

    override suspend fun loadPlaylistUri(uri: String?): Flow<Result<MediaListItem>> =
        flow<Result<MediaListItem>> {
            emit(
                Result.failure(
            Exception("App is in offline mode")
        ))
        }.flowOn(Dispatchers.IO)

    override suspend fun loadAutoPlaylistUri(uri: String?): Flow<Result<MediaListItem>> =
        flow<Result<MediaListItem>> {
            emit(
                Result.failure(
            Exception("App is in offline mode")
        ))
        }.flowOn(Dispatchers.IO)

    override suspend fun search(query: String): Flow<Result<MediaListItem>> =
        flow<Result<MediaListItem>> {
            playlistDao.getLikedSongs()?.songs?.forEach {
            Result.success(it.toMediaListItem())
            }
        }.flowOn(Dispatchers.IO)
}