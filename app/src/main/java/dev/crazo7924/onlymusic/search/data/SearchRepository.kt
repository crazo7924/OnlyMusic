package dev.crazo7924.onlymusic.search.data

interface SearchRepository {
    suspend fun search(query: String): Result<List<SearchSuggestion>>
}


