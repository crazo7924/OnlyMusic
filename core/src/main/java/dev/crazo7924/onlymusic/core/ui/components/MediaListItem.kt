package dev.crazo7924.onlymusic.core.ui.components

import org.schabi.newpipe.extractor.InfoItem

data class MediaListItem(
    val title: String?,
    val artist: String? = null,
    val infoType: InfoItem.InfoType,
    val thumbnailUri: String?,
    val mediaUri: String? = null,
    val duration: Long? = 0L,
)