package dev.crazo7924.onlymusic.features.search

import dev.crazo7924.onlymusic.core.ui.components.MediaListItem

enum class SearchState {
    INITIAL,
    SEARCHING,
    LOADING,
    SUCCESS,
    ERROR
}

data class SearchUiState(
    val query: String = "",
    val suggestions: List<MediaListItem> = listOf(),
    val searchState: SearchState = SearchState.INITIAL,
)