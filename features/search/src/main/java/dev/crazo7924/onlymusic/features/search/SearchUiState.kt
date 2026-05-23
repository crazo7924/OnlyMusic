/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.search

import dev.crazo7924.onlymusic.core.MediaListItem

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