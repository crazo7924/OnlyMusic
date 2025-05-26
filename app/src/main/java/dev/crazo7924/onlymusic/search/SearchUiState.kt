package dev.crazo7924.onlymusic.search

import dev.crazo7924.onlymusic.MediaListItem

enum class SearchState {
    INITIAL,
    SEARCHING,
    SUCCESS,
    ERROR
}

data class SearchUiState(
    val query: String = "",
    val suggestions: List<MediaListItem> = listOf(),
    val searchState: SearchState = SearchState.INITIAL,
    val error: Throwable? = null
)