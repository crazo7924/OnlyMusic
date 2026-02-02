package dev.crazo7924.onlymusic.features.player

import androidx.media3.common.MediaItem

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState.INITIAL,
    val currentMediaItemIndex: Int = 0,
    val position: Long = 0L,
    val errorMessage: String? = null,
    val queue: List<MediaItem> = listOf(),
    val media: MediaItem? = null,
    val duration: Long? = 0L,
)

enum class PlaybackState {
    INITIAL,
    LOADING,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR
}