package com.sigma67.ytmusicapi.models

data class PlaylistItem(
    val videoId: String,
    val title: String,
    val artists: List<Artist> = emptyList(),
    val album: Album? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val thumbnails: List<Thumbnail>? = null,
    val setVideoId: String? = null,
    val feedbackTokens: Map<String, String>? = null,
)
