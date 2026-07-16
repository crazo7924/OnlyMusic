/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.db

import androidx.room.Entity
import androidx.room.Index
import java.util.UUID

@Entity(
    primaryKeys = ["songId", "artistId"],
    indices = [Index(value = ["artistId"])]
)
data class SongArtistCrossRef(
    val songId: String,
    val artistId: UUID,
)
