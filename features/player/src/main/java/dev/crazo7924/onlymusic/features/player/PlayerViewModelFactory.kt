package dev.crazo7924.onlymusic.features.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PlayerViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java))
            return PlayerViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}