package dev.crazo7924.onlymusic.player

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(value = PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun setPlaybackState(isPlaying: Boolean) {
        _uiState.update {
            it.copy(playbackState = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED)
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

    fun setStopped() {
        _uiState.update {
            it.copy(playbackState = PlaybackState.STOPPED)
        }
    }

    fun setCurrentMediaItem(mediaItem: MediaItem?) {
        _uiState.update {
            it.copy(media = mediaItem)
        }
    }

    fun enqueue(mediaListItem: MediaItem) {
        _uiState.update {
            it.copy(queue = it.queue + mediaListItem)
        }
    }

    fun updateQueue(queue: List<MediaItem>) {
        _uiState.update {
            it.copy(queue = queue)
        }
    }

    fun setQueueItemIndex(index: Int) {
        _uiState.update {
            it.copy(
                currentMediaItemIndex = index,
                media = if (it.queue.isNotEmpty()) it.queue[index] else null,
            )
        }
    }

    fun updateDuration(duration: Long) {
        _uiState.update {
            it.copy(duration = duration)
        }

    }

    fun setCurrentMediaItemIndex(currentMediaItemIndex: Int) {
        _uiState.update {
            it.copy(currentMediaItemIndex = currentMediaItemIndex)
        }

    }

    fun setReady() {
        _uiState.update {
            it.copy(playbackState = PlaybackState.INITIAL)
        }
    }

    fun setError(errorMessage: String?) {
        _uiState.update {
            it.copy(playbackState = PlaybackState.ERROR, errorMessage = errorMessage)
        }
    }
}