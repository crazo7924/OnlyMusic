package dev.crazo7924.onlymusic.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel
class PlayerViewModel(private val player: ExoPlayer) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _uiState.update {
                    it.copy(
                        media = mediaItem
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update {
                    it.copy(playbackState = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING,
                    Player.STATE_READY,
                    Player.STATE_IDLE -> _uiState.update {
                        it.copy(
                            playbackState = PlaybackState.LOADING
                        )
                    }

                    Player.STATE_ENDED -> onPlayNext()
                }
            }
        })
    }

    // Load Playlist
    fun loadPlaylist(playlist: ArrayDeque<MediaItem>) {
        _uiState.update { it.copy(queue = playlist) }
        player.setMediaItems(playlist)
        player.prepare()
    }

    // Play/Pause
    fun onPlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    // Play Next
    fun onPlayNext() {
        player.seekToNextMediaItem()
        player.play()
    }

    // Play Previous
    fun onPlayPrevious() {
        player.seekToPreviousMediaItem()
        player.play()
    }

    // Seek to
    fun onSeekTo(position: Float) {
        player.pause()
        player.seekTo((player.duration * position).toLong())
        player.play()
    }

    // update player current position
    fun updatePlayerPosition() {
        viewModelScope.launch {
            while (player.isPlaying) {
                _uiState.update {
                    it.copy(
                        position = player.currentPosition,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}