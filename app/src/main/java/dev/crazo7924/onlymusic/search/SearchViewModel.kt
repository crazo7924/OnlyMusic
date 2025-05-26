package dev.crazo7924.onlymusic.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.crazo7924.onlymusic.search.data.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository,
    val minQueryLength: Int = 2,
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(
            query = "",
            searchState = SearchState.INITIAL,
            suggestions = listOf()
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun updateQueryFrom(updatedValue: String) {
//        if (updatedValue.isBlank() || updatedValue.length < minQueryLength) {
//            _uiState.update { it.copy(suggestions = listOf()) }
//        }
        _uiState.update { it.copy(query = updatedValue) }
    }

    fun search() {
        _uiState.update { it.copy(searchState = SearchState.SEARCHING) }
        if (uiState.value.query.length < minQueryLength) {
            _uiState.update {
                it.copy(suggestions = listOf(), searchState = SearchState.INITIAL)
            }
            return
        }
        viewModelScope.launch {
            val suggestionsResult = searchRepository.search(uiState.value.query)
            suggestionsResult.onSuccess {
                data ->
                _uiState.update {
                    it.copy(
                        searchState = SearchState.SUCCESS,
                        suggestions = data,
                        error = null
                    )
                }
            }
            suggestionsResult.onFailure{
                throwable ->
                _uiState.update {
                    it.copy(
                        searchState = SearchState.ERROR,
                        suggestions = listOf(),
                        error = throwable
                    )
                }
            }
        }
    }
}
