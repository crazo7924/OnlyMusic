package dev.crazo7924.onlymusic.search.data

import org.schabi.newpipe.extractor.InfoItem.InfoType

fun InfoType.toTypeString(): String {
    return when (this) {
        InfoType.STREAM -> "Song"
        InfoType.PLAYLIST -> "Album"
        InfoType.CHANNEL -> "Artist"
        InfoType.COMMENT -> "Comment (wtf)"
    }
}