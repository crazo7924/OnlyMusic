/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun loadMediaUri(uri: String?): Result<MediaListItem>
    suspend fun loadPlaylistUri(uri: String?): Flow<Result<MediaListItem>>

    suspend fun loadAutoPlaylistUri(uri: String?): Flow<Result<MediaListItem>>
    suspend fun loadMorePlaylistItems(): Flow<Result<MediaListItem>>
    suspend fun search(query: String): Flow<Result<MediaListItem>>
    suspend fun getRecentSongs(): Flow<List<MediaListItem>>

    suspend fun getSearchSuggestions(query: String): Result<List<String>>
    suspend fun getRecentQueries(): Flow<List<String>>
    suspend fun addRecentQuery(query: String)
    suspend fun deleteRecentQuery(query: String)
}
