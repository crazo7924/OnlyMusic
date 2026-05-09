package com.sigma67.ytmusicapi.models

data class Album(
    val title: String,
    val browseId: String,
    val artist: Artist? = null,
    val year: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val duration: String? = null,
    val trackCount: String? = null,
)