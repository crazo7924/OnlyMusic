package com.sigma67.ytmusicapi.models

data class Playlist(
    val title: String,
    val playlistId: String,
    val description: String? = null,
    val author: Artist? = null,
    val thumbnails: List<Thumbnail>? = null,
    val count: String? = null,
    val duration: String? = null,
)