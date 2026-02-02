package dev.crazo7924.onlymusic

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import dev.crazo7924.onlymusic.service.PlayerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaControllerManager(
    private val context: Context,
    private val playerViewModel: PlayerViewModel,
    private val lifecycleScope: LifecycleCoroutineScope,
) {
    // For testing
    internal var mediaControllerBuilderFactory: (Context, SessionToken) -> MediaController.Builder =
        { context, sessionToken ->
            MediaController.Builder(context, sessionToken)
        }

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null


    fun initialize() {
        if (mediaControllerFuture != null) {
            return
        }

        val sessionToken = SessionToken(
            context, ComponentName(context, PlayerService::class.java)
        )

        mediaControllerFuture = mediaControllerBuilderFactory(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener(
            {
                val mediaController = mediaControllerFuture?.get() ?: return@addListener
                Log.d(TAG, "MediaController connected.")

                // Initial state that might have been missed before connection
                playerViewModel.updateStateFromPlayer(mediaController)

                mediaController.addListener(PlayerListener(playerViewModel, lifecycleScope))
            },
            MoreExecutors.directExecutor()
        )
    }

    fun release() {
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
            Log.d(TAG, "MediaController released.")
        }
        mediaControllerFuture = null
    }

    fun getController(): MediaController? {
        if (mediaControllerFuture?.isDone == true) {
            return mediaControllerFuture?.get()
        }
        return null
    }

    private class PlayerListener(
        private val playerViewModel: PlayerViewModel,
        private val coroutineScope: LifecycleCoroutineScope,
    ) : Player.Listener {

        private var positionUpdateJob: Job? = null

        private fun stopPositionUpdates() {
            positionUpdateJob?.cancel()
            positionUpdateJob = null
        }

        private fun startPositionUpdates(player: Player) {
            stopPositionUpdates() // Ensure only one job is running
            positionUpdateJob = coroutineScope.launch {
                while (isActive) {
                    // ALWAYS use the 'player' instance from the listener context
                    playerViewModel.updatePosition(player.currentPosition.coerceAtLeast(0L))
                    playerViewModel.updateDuration(player.duration.coerceAtLeast(0L))
                    delay(UPDATE_DELAY)
                }
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)

            // Update everything that might have changed, using the fresh 'player' instance
            playerViewModel.updateStateFromPlayer(player)

            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                if (player.isPlaying) {
                    startPositionUpdates(player)
                } else {
                    stopPositionUpdates()
                }
            }
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                // If the player stops or ends, stop polling for position.
                if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                    stopPositionUpdates()
                }
            }
            if (events.contains(Player.EVENT_PLAYER_ERROR)) {
                Log.e(TAG, "Listener: onPlayerError", player.playerError)
                playerViewModel.setError(player.playerError?.message)
                stopPositionUpdates()
            }
        }
    }

    companion object {
        private const val TAG = "MediaControllerManager"
        const val UPDATE_DELAY = 500L
    }
}
