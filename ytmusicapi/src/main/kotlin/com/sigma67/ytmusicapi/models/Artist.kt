package com.sigma67.ytmusicapi.models

data class Artist(
    val name: String,
    val id: String? = null,
    val browseId: String? = null,
    val thumbnails: List<Thumbnail>? = null,
)