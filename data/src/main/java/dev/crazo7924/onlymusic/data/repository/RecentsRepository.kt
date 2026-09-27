/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import kotlinx.coroutines.flow.Flow

interface RecentsRepository {
    suspend fun getRecentSongs(): Flow<List<MediaListItem>>
    suspend fun saveToRecents(mediaListItem: MediaListItem)
}
