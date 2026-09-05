/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
) : ViewModel() {

    val minQueryLength: Int = 2

    private val _uiState = MutableStateFlow(
        SearchUiState(
            query = "",
            searchState = SearchState.INITIAL,
            suggestions = listOf()
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var suggestionsJob: Job? = null

    init {
        viewModelScope.launch {
            musicRepository.getRecentSongs().collect { songs ->
                _uiState.update { it.copy(recentSongs = songs) }
            }
        }
        viewModelScope.launch {
            musicRepository.getRecentQueries().collect { queries ->
                _uiState.update { it.copy(recentQueries = queries) }
            }
        }
    }

    fun updateQueryFrom(updatedValue: String) {
        suggestionsJob?.cancel()
        if (updatedValue.isBlank()) {
            _uiState.update {
                it.copy(
                    query = updatedValue,
                    suggestions = listOf(),
                    querySuggestions = listOf(),
                    searchState = SearchState.INITIAL
                )
            }
            return
        }

        _uiState.update { it.copy(query = updatedValue) }

        suggestionsJob = viewModelScope.launch {
            val result = musicRepository.getSearchSuggestions(updatedValue)
            result.onSuccess { suggestions ->
                if (_uiState.value.query == updatedValue) {
                    _uiState.update { it.copy(querySuggestions = suggestions) }
                }
            }
        }
    }

    fun deleteRecentQuery(query: String) {
        viewModelScope.launch {
            musicRepository.deleteRecentQuery(query)
        }
    }

    fun search() {
        val currentQuery = uiState.value.query.trim()
        _uiState.update { it.copy(searchState = SearchState.SEARCHING) }
        if (currentQuery.length < minQueryLength) {
            _uiState.update {
                it.copy(suggestions = listOf(), searchState = SearchState.INITIAL)
            }
            return
        }

        viewModelScope.launch {
            musicRepository.addRecentQuery(currentQuery)
        }

        viewModelScope.launch {
            val suggestionsResult = musicRepository.search(currentQuery)

            // first reset the suggestions list
            _uiState.update {
                it.copy(
                    suggestions = listOf(),
                    searchState = SearchState.INITIAL
                )
            }
            suggestionsResult.collect { resultItem ->
                resultItem.onSuccess { media ->
                    _uiState.update {
                        it.copy(
                            searchState = SearchState.LOADING,
                            suggestions = it.suggestions + media,
                        )
                    }
                }
            }

            if (_uiState.value.suggestions.isEmpty()) {
                _uiState.update {
                    it.copy(searchState = SearchState.ERROR)
                }
                return@launch
            }

            _uiState.update {
                it.copy(searchState = SearchState.SUCCESS)
            }
        }
    }
}
