package dev.crazo7924.onlymusic.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.crazo7924.onlymusic.repository.MusicRepository

class SearchViewModelFactory(private val musicRepository: MusicRepository, private val minQueryLength: Int  = 3) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(musicRepository, minQueryLength = minQueryLength) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}