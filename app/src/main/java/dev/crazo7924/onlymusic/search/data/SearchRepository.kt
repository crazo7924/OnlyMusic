package dev.crazo7924.onlymusic.search.data

import dev.crazo7924.onlymusic.MediaListItem

interface SearchRepository {
    suspend fun search(query: String): Result<List<MediaListItem>>
}


