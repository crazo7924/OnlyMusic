package dev.crazo7924.onlymusic.player

import androidx.media3.common.MediaItem

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState.INITIAL,
    val media: MediaItem? = null,
    val position: Long = 0L,
    val duration: Long = media?.mediaMetadata?.durationMs ?: 0L,
    val error: String? = null,
)

enum class PlaybackState {
    INITIAL,
    LOADING,
    PLAYING,
    PAUSED,
    STOPPED
}