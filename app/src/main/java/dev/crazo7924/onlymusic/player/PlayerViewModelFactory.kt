package dev.crazo7924.onlymusic.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer

class PlayerViewModelFactory(private val player: ExoPlayer) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java))
            return PlayerViewModel(player) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}