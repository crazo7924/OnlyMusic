package dev.crazo7924.onlymusic.features.player

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(value = PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun updatePosition(position: Long) {
        _uiState.update {
            it.copy(position = position)
        }
    }

    fun updateDuration(duration: Long) {
        _uiState.update {
            it.copy(duration = duration)
        }

    }

    fun setError(errorMessage: String?) {
        _uiState.update {
            it.copy(playbackState = PlaybackState.ERROR, errorMessage = errorMessage)
        }
    }
    /**
     * Updates the various state values from the player object itself.
     *
     * Note that [Player.playbackState] cannot tell about the pause state
     * hence it must be handled separately using [Player.playWhenReady].
     */
    fun updateStateFromPlayer(player: Player) {
        _uiState.update {
            it.copy(
                playbackState = when (player.playbackState) {
                    Player.STATE_BUFFERING -> PlaybackState.LOADING
                    Player.STATE_READY -> PlaybackState.INITIAL
                    Player.STATE_ENDED -> PlaybackState.STOPPED
                    else -> PlaybackState.INITIAL
                },
                media = player.currentMediaItem,
                currentMediaItemIndex = player.currentMediaItemIndex,
                queue = player.currentTimeline.toMediaItems(),
                position = player.currentPosition,
                duration = player.duration,
                errorMessage = player.playerError?.message,
            )
        }

        _uiState.update {
            it.copy(
                playbackState = if (player.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
            )
        }
    }
}

private fun Timeline.toMediaItems(): List<MediaItem> {
    val mediaItems = mutableListOf<MediaItem>()
    if (this.windowCount > 0) {
        for (i in 0 until this.windowCount) {
            val window = Timeline.Window()
            this.getWindow(i, window)
            mediaItems.add(window.mediaItem)
        }
    }
    return mediaItems
}