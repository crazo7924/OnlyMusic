package com.sigma67.ytmusicapi.models

data class Song(
    val videoId: String,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val thumbnails: List<Thumbnail>? = null,
    val videoType: String? = null,
    val isAvailable: Boolean = true,
    val isExplicit: Boolean = false,
)