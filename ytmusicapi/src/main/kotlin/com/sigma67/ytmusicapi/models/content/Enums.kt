package com.sigma67.ytmusicapi.models.content

/**
 * Content enums for ytmusicapi
 */

enum class PrivacyStatus(val value: String) {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    UNLISTED("UNLISTED")
}

enum class LikeStatus(val value: String) {
    LIKE("LIKE"),
    DISLIKE("DISLIKE"),
    INDIFFERENT("INDIFFERENT");

    companion object {
        fun fromValue(value: String?): LikeStatus {
            return entries.find { it.value == value } ?: INDIFFERENT
        }
    }
}

enum class VideoType(val value: String) {
    OMV("MUSIC_VIDEO_TYPE_OMV"),
    UGC("MUSIC_VIDEO_TYPE_UGC"),
    ATV("MUSIC_VIDEO_TYPE_ATV"),
    OFFICIAL_SOURCE_MUSIC("MUSIC_VIDEO_TYPE_OFFICIAL_SOURCE_MUSIC")
}
