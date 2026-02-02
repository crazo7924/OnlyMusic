package dev.crazo7924.onlymusic.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.TaskStackBuilder
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.data.repository.NewPipeMusicRepository
import dev.crazo7924.onlymusic.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerService : MediaSessionService() {

    companion object {
        const val TAG = "PlayerService"

        // Custom SessionCommands
        val COMMAND_LOAD_STREAM_URI =
            SessionCommand("dev.crazo7924.onlymusic.player.LOAD_STREAM_URI", Bundle.EMPTY)
        val COMMAND_LOAD_PLAYLIST_URI =
            SessionCommand("dev.crazo7924.onlymusic.player.LOAD_PLAYLIST_URI", Bundle.EMPTY)
        val COMMAND_ENQUEUE_URI =
            SessionCommand("dev.crazo7924.onlymusic.player.ENQUEUE_URI", Bundle.EMPTY)

        val COMMAND_ENQUEUE_NEXT_URI =
            SessionCommand("dev.crazo7924.onlymusic.player.ENQUEUE_NEXT_URI", Bundle.EMPTY)
        val COMMAND_ENQUEUE_PLAYLIST_URI =
            SessionCommand("dev.crazo7924.onlymusic.player.ENQUEUE_PLAYLIST_URI", Bundle.EMPTY)
        val COMMAND_SEEK_TO_PERCENTAGE =
            SessionCommand("dev.crazo7924.onlymusic.player.SEEK_TO_PERCENTAGE", Bundle.EMPTY)

        val COMMAND_ENQUEUE_RADIO =
            SessionCommand("dev.crazo7924.onlymusic.player.START_RADIO", Bundle.EMPTY)

        // Keys for Bundle arguments
        const val KEY_URI = "KEY_URI"
        const val KEY_PLAYLIST_URI = "KEY_PLAYLIST_URI"
        const val KEY_PLAY_WHEN_READY = "KEY_PLAY_WHEN_READY"
        const val KEY_PERCENTAGE = "KEY_PERCENTAGE"

        // Intent Extras for onStartCommand (for initial media loading)
        const val EXTRA_INITIAL_STREAM_URI = "EXTRA_INITIAL_STREAM_URI"
        const val EXTRA_INITIAL_PLAYLIST_URI = "EXTRA_INITIAL_PLAYLIST_URI"
        const val EXTRA_INITIAL_AUTO_PLAY = "EXTRA_INITIAL_AUTO_PLAY" // Boolean
    }

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var musicRepository: MusicRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaSessionCallback: PlayerMediaSessionCallback


    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        musicRepository = NewPipeMusicRepository() // Replace with DI if available

        mediaSessionCallback = PlayerMediaSessionCallback()

        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent!!)
            .setCallback(mediaSessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand received. Intent: $intent")

        intent?.let {
            val initialStreamUri = it.getStringExtra(EXTRA_INITIAL_STREAM_URI)
            val initialPlaylistUri = it.getStringExtra(EXTRA_INITIAL_PLAYLIST_URI)
            val autoPlay = it.getBooleanExtra(EXTRA_INITIAL_AUTO_PLAY, false)

            when {
                initialStreamUri != null -> {
                    Log.d(
                        TAG,
                        "onStartCommand: Handling initial Stream URI $initialStreamUri, autoPlay: $autoPlay"
                    )
                    mediaSessionCallback.processLoadStreamUri(initialStreamUri, autoPlay)
                }

                initialPlaylistUri != null -> {
                    Log.d(
                        TAG,
                        "onStartCommand: Handling initial Playlist URI $initialPlaylistUri, autoPlay: $autoPlay"
                    )
                    mediaSessionCallback.processLoadPlaylistUri(initialPlaylistUri, autoPlay)
                }
            }
        }
        return START_STICKY
    }


    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class PlayerMediaSessionCallback : MediaSession.Callback {

        fun processLoadStreamUri(uri: String, playWhenReady: Boolean) {
            serviceScope.launch {
                val result = musicRepository.loadMediaUri(uri)
                result.onSuccess { item ->
                    withContext(Dispatchers.Main) {
                        exoPlayer.setMediaItem(item.toMediaItem())
                        exoPlayer.prepare()
                        if (playWhenReady) exoPlayer.play()
                        Log.d(TAG, "Stream URI loaded. Play when ready: $playWhenReady")
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error loading stream URI $uri: $error")
                }
            }
        }

        fun processLoadPlaylistUri(playlistUri: String, playWhenReady: Boolean) {
            serviceScope.launch {
                exoPlayer.clearMediaItems()
                val results = musicRepository.loadPlaylistUri(playlistUri)
                val mediaItems = mutableListOf<MediaItem>()
                results.forEach { result ->
                    result.onSuccess { mediaItems.add(it.toMediaItem()) }
                        .onFailure { error ->
                            Log.e(
                                TAG,
                                "Error loading item from playlist $playlistUri: $error"
                            )
                        }
                }
                if (mediaItems.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        exoPlayer.setMediaItems(mediaItems)
                        exoPlayer.prepare()
                        if (playWhenReady) exoPlayer.play()
                        Log.d(TAG, "Playlist URI loaded. Play when ready: $playWhenReady")
                    }
                } else {
                    Log.w(TAG, "No media items successfully loaded from playlist URI $playlistUri")
                }
            }
        }

        fun processEnqueueRadio(mediaUri: String) {
            serviceScope.launch {
                Log.d(TAG, "Radio URI received: $mediaUri")
                val loadedMediaItems = musicRepository.loadAutoPlaylistUri(mediaUri)
                if (loadedMediaItems.isEmpty()) {
                    Log.w(TAG, "No media items successfully loaded from radio URI $mediaUri")
                    return@launch
                }

                loadedMediaItems.forEach { result ->
                    result.onSuccess { item ->
                        withContext(Dispatchers.Main) {
                            exoPlayer.addMediaItem(item.toMediaItem())
                        }
                    }
                }

                Log.d(TAG, "processEnqueueRadio: successfully loaded radio for URI: $mediaUri")
            }
        }

        fun processEnqueueUri(uri: String) {
            serviceScope.launch {
                val result = musicRepository.loadMediaUri(uri)
                result.onSuccess { item ->
                    withContext(Dispatchers.Main) {
                        exoPlayer.addMediaItem(item.toMediaItem())
                        Log.d(TAG, "URI enqueued.")
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error enqueueing URI $uri: $error")
                }
            }
        }

        fun processEnqueuePlaylistUri(playlistUri: String) {
            serviceScope.launch {
                val results = musicRepository.loadPlaylistUri(playlistUri)
                val mediaItems = mutableListOf<MediaItem>()
                results.forEach { result ->
                    result.onSuccess { mediaItems.add(it.toMediaItem()) }
                        .onFailure { error ->
                            Log.e(
                                TAG,
                                "Error loading item from playlist for enqueue $playlistUri: $error"
                            )
                        }
                }
                if (mediaItems.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        exoPlayer.addMediaItems(mediaItems)
                        Log.d(TAG, "Playlist URI enqueued.")
                    }
                } else {
                    Log.w(
                        TAG,
                        "No media items successfully loaded from playlist URI for enqueue $playlistUri"
                    )
                }
            }
        }

        fun processSeekToPercentage(percentage: Float) {
            if (percentage !in 0f..1f) {
                Log.d(TAG, "processSeekToPercentage: incorrect percentage value: $percentage")
                return
            }
            val duration = exoPlayer.duration
            if (duration == C.TIME_UNSET) {  // Check against TIME_UNSET for invalid duration
                Log.d(TAG, "processSeekToPercentage: duration is not available for seek.")
            } else {
                val newPosition = (percentage * duration).toLong()
                exoPlayer.seekTo(newPosition)
                Log.d(TAG, "Media position changed to $newPosition ms (percentage: $percentage)")
            }
        }

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "MediaSession.Callback: onConnect from ${controller.packageName}")
            val availableSessionCommands = SessionCommands.Builder()
                .addSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.commands)
                .add(COMMAND_LOAD_STREAM_URI)
                .add(COMMAND_LOAD_PLAYLIST_URI)
                .add(COMMAND_ENQUEUE_URI)
                .add(COMMAND_ENQUEUE_NEXT_URI)
                .add(COMMAND_ENQUEUE_PLAYLIST_URI)
                .add(COMMAND_SEEK_TO_PERCENTAGE)
                .add(COMMAND_ENQUEUE_RADIO)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands)
                .build()
        }

        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            Log.d(
                TAG,
                "onCustomCommand: ${customCommand.customAction} from ${controller.packageName}"
            )
            when (customCommand) {
                COMMAND_LOAD_STREAM_URI -> {
                    val uri = args.getString(KEY_URI)
                    val playWhenReady = args.getBoolean(KEY_PLAY_WHEN_READY, false)
                    if (uri != null) {
                        processLoadStreamUri(uri, playWhenReady)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_LOAD_PLAYLIST_URI -> {
                    val playlistUri = args.getString(KEY_PLAYLIST_URI)
                    val playWhenReady = args.getBoolean(KEY_PLAY_WHEN_READY, false)
                    if (playlistUri != null) {
                        processLoadPlaylistUri(playlistUri, playWhenReady)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_ENQUEUE_URI -> {
                    val uri = args.getString(KEY_URI)
                    if (uri != null) {
                        processEnqueueUri(uri)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_ENQUEUE_NEXT_URI -> {
                    val uri = args.getString(KEY_URI)
                    if (uri != null) {
                        processEnqueueNext(uri)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_ENQUEUE_PLAYLIST_URI -> {
                    val playlistUri = args.getString(KEY_PLAYLIST_URI)
                    if (playlistUri != null) {
                        processEnqueuePlaylistUri(playlistUri)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_SEEK_TO_PERCENTAGE -> {
                    val percentage = args.getFloat(KEY_PERCENTAGE, -1f)
                    if (percentage != -1f) {
                        processSeekToPercentage(percentage)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }

                COMMAND_ENQUEUE_RADIO -> {
                    val mediaUri = args.getString(KEY_URI)
                    if (mediaUri != null) {
                        processEnqueueRadio(mediaUri)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionError.ERROR_UNKNOWN)) // Or specific error
        }

        private fun processEnqueueNext(uri: String) {
            serviceScope.launch {
                val result = musicRepository.loadMediaUri(uri)
                result.onSuccess { mediaItem ->
                    withContext(Dispatchers.Main) {
                        exoPlayer.addMediaItem(
                            exoPlayer.currentMediaItemIndex + 1,
                            mediaItem.toMediaItem()
                        )
                        Log.d(TAG, "URI enqueued for next.")
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error enqueueing next URI $uri: $error")
                }
            }
        }
    }
}