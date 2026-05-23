/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class SongWithArtists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "songId",
        entityColumn = "artistId"
    )
    val artists: List<Artist>
)
