package dev.crazo7924.onlymusic.core.ui.components

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.schabi.newpipe.extractor.InfoItem

data class MediaListItem(
    val title: String?,
    val artist: String? = null,
    val infoType: InfoItem.InfoType,
    val thumbnailUri: String?,
    val mediaUri: String? = null,
    val duration: Long? = 0L,
) {
    fun toMediaItem(): MediaItem {
        val mediaItemBuilder = MediaItem.Builder()
        val mediaMetadataBuilder = MediaMetadata.Builder()
        mediaMetadataBuilder.setArtist(artist)
        mediaMetadataBuilder.setTitle(title)
        mediaMetadataBuilder.setArtworkUri(thumbnailUri?.toUri())
        mediaMetadataBuilder.setDurationMs(duration)
        mediaItemBuilder.setMediaMetadata(mediaMetadataBuilder.build())
        mediaItemBuilder.setUri(mediaUri?.toUri())
        return mediaItemBuilder.build()
    }
}

fun MediaItem.toMediaListItem(): MediaListItem {
    return MediaListItem(
        title = this.mediaMetadata.title?.toString(),
        artist = this.mediaMetadata.artist?.toString(),
        infoType = InfoItem.InfoType.STREAM,
        thumbnailUri = this.mediaMetadata.artworkUri?.toString(),
        mediaUri = this.localConfiguration?.uri?.toString(),
        duration = this.mediaMetadata.durationMs
    )
}