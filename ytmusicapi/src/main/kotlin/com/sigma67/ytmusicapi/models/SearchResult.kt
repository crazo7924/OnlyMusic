package com.sigma67.ytmusicapi.models

data class SearchResult(
    val category: String? = null,
    val resultType: String,
    val title: String,
    val artists: List<Artist>? = null,
    val album: Album? = null,
    val videoId: String? = null,
    val playlistId: String? = null,
    val browseId: String? = null,
    val views: String? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val thumbnails: List<Thumbnail>? = null,
)