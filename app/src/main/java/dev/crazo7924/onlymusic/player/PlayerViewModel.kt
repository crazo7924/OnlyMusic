package dev.crazo7924.onlymusic.player

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(value = PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun onPlayPauseToggled(wasPlaying: Boolean) {
        _uiState.update {
            it.copy(
                playbackState = if (wasPlaying) PlaybackState.PAUSED else PlaybackState.PLAYING
            )
        }
    }

    fun restorePlayPauseToggle(isPlaying: Boolean) {
        _uiState.update {
            it.copy(playbackState = if(isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED)
        }
    }

    fun updatePosition(position: Long) {
        _uiState.update {
            it.copy(position = position)
        }
    }

    fun setLoading() {
        _uiState.update {
            it.copy(playbackState = PlaybackState.LOADING)
        }
    }

    fun setMediaItem(mediaItem: MediaItem?) {
        _uiState.update {
            it.copy(media = mediaItem, queue = listOf())
        }
    }
}