/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.SongWithArtists
import org.schabi.newpipe.extractor.InfoItem

fun SongWithArtists.toMediaListItem(): MediaListItem {
    return MediaListItem(
        id = this.song.songId,
        title = this.song.title,
        artist = this.artists.joinToString { it.name },
        infoType = InfoItem.InfoType.STREAM, // It is always a Song
        thumbnailUri = this.song.artworkUri?.toString(),
        mediaUri = this.song.uri.toString(),
        duration = this.song.duration
    )
}
