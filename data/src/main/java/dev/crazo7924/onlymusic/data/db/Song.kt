/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI
import java.util.UUID

@Entity
data class Song(
    @PrimaryKey val songId: String = UUID.randomUUID().toString(),
    val title: String,
    val uri: URI,
    val artworkUri: URI?,
    val duration: Long,
)
