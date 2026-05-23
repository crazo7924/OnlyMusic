/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.core

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.schabi.newpipe.extractor.InfoItem

data class MediaListItem(
    val id: String,
    val title: String?,
    val artist: String? = null,
    val infoType: InfoItem.InfoType,
    val thumbnailUri: String?,
    val mediaUri: String? = null,
    val duration: Long? = 0L,
)

fun MediaListItem.toMediaItem(): MediaItem {
    val mediaItemBuilder = MediaItem.Builder()
    val mediaMetadataBuilder = MediaMetadata.Builder()
    mediaItemBuilder.setMediaId(id)
    mediaMetadataBuilder.setArtist(artist)
    mediaMetadataBuilder.setTitle(title)
    mediaMetadataBuilder.setArtworkUri(thumbnailUri?.toUri())
    mediaMetadataBuilder.setDurationMs(duration)
    mediaItemBuilder.setMediaMetadata(mediaMetadataBuilder.build())
    mediaItemBuilder.setUri(mediaUri?.toUri())
    return mediaItemBuilder.build()
}

fun MediaItem.toMediaListItem(): MediaListItem {
    return MediaListItem(
        id = this.mediaId,
        title = this.mediaMetadata.title?.toString(),
        artist = this.mediaMetadata.artist?.toString(),
        infoType = InfoItem.InfoType.STREAM,
        thumbnailUri = this.mediaMetadata.artworkUri?.toString(),
        mediaUri = this.localConfiguration?.uri?.toString(),
        duration = this.mediaMetadata.durationMs
    )
}