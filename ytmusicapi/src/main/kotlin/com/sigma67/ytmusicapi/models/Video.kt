package com.sigma67.ytmusicapi.models

data class Video(
    val videoId: String,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val thumbnails: List<Thumbnail>? = null,
    val videoType: String? = null,
    val views: String? = null,
)