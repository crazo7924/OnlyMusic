/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun search(query: String): Flow<Result<MediaListItem>>
    suspend fun getSearchSuggestions(query: String): Result<List<String>>
}
