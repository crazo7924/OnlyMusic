package dev.crazo7924.onlymusic.search

import dev.crazo7924.onlymusic.search.data.SearchSuggestion

enum class SearchState {
    INITIAL,
    SEARCHING,
    SUCCESS,
    ERROR
}

data class SearchUiState(
    val query: String,
    val suggestions: List<SearchSuggestion>,
    val searchState: SearchState,
)