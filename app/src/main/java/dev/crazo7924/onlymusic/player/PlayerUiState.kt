package dev.crazo7924.onlymusic.player

import androidx.media3.common.MediaItem

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState.INITIAL,
    val media: MediaItem? = null,
    val position: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null,
    val queue: ArrayDeque<MediaItem> = ArrayDeque(),
)

enum class PlaybackState {
    INITIAL,
    LOADING,
    PLAYING,
    PAUSED,
    STOPPED
}