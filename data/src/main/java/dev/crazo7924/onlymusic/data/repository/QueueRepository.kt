/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem

interface QueueRepository {
    suspend fun saveQueue(items: List<MediaListItem>, activeIndex: Int, positionMs: Long)
    suspend fun getSavedQueue(): SavedQueueState?
}

data class SavedQueueState(
    val items: List<MediaListItem>,
    val activeIndex: Int,
    val positionMs: Long,
)
