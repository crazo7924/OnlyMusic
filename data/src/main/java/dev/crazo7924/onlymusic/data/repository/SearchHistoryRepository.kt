/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun getRecentQueries(): Flow<List<String>>
    suspend fun addRecentQuery(query: String)
    suspend fun deleteRecentQuery(query: String)
}
