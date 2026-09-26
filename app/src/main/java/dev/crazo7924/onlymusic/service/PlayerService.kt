/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.TaskStackBuilder
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
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
import dagger.hilt.android.AndroidEntryPoint
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.core.toMediaItem
import dev.crazo7924.onlymusic.core.toMediaListItem
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
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

        val COMMAND_LOAD_MORE_QUEUE =
            SessionCommand("dev.crazo7924.onlymusic.player.LOAD_MORE_QUEUE", Bundle.EMPTY)

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
    @Inject
    lateinit var musicRepository: MusicRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaSessionCallback: PlayerMediaSessionCallback
    private lateinit var customCommandHandler: PlayerCustomCommandHandler

    private var recentJob: Job? = null
    private var currentMediaItemForRecent: MediaItem? = null

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            resetRecentTimer(mediaItem)
            saveCurrentQueueState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                val currentMedia = exoPlayer.currentMediaItem
                if (recentJob == null && currentMedia != null && currentMedia != currentMediaItemForRecent) {
                    startRecentTimer(currentMedia)
                }
            } else {
                recentJob?.cancel()
                recentJob = null
                saveCurrentQueueState()
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            saveCurrentQueueState()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            saveCurrentQueueState()
        }
    }

    private fun saveCurrentQueueState() {
        if (!::exoPlayer.isInitialized) return
        val count = exoPlayer.mediaItemCount
        val items = mutableListOf<MediaListItem>()
        for (i in 0 until count) {
            items.add(exoPlayer.getMediaItemAt(i).toMediaListItem())
        }
        val activeIndex = exoPlayer.currentMediaItemIndex.coerceAtLeast(0)
        val positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)

        serviceScope.launch {
            musicRepository.saveQueue(items, activeIndex, positionMs)
        }
    }

    private fun restoreQueueState() {
        serviceScope.launch {
            val savedQueue = musicRepository.getSavedQueue() ?: return@launch
            if (savedQueue.items.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val mediaItems = savedQueue.items.map { it.toMediaItem() }
                    exoPlayer.setMediaItems(
                        mediaItems,
                        savedQueue.activeIndex.coerceIn(0, mediaItems.size - 1),
                        savedQueue.positionMs
                    )
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = false
                    Log.d(
                        TAG,
                        "Queue restored with ${mediaItems.size} items at index ${savedQueue.activeIndex}."
                    )
                }
            }
        }
    }

    private fun resetRecentTimer(mediaItem: MediaItem?) {
        recentJob?.cancel()
        recentJob = null
        currentMediaItemForRecent = null
        if (mediaItem != null && exoPlayer.isPlaying) {
            startRecentTimer(mediaItem)
        }
    }

    private fun startRecentTimer(mediaItem: MediaItem) {
        recentJob = serviceScope.launch {
            delay(3000.milliseconds)
            musicRepository.saveToRecents(mediaItem.toMediaListItem())
            currentMediaItemForRecent = mediaItem
        }
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer.addListener(playerListener)

        customCommandHandler = PlayerCustomCommandHandler(exoPlayer, musicRepository, serviceScope)
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

        restoreQueueState()
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
                    customCommandHandler.processLoadStreamUri(initialStreamUri, autoPlay)
                }

                initialPlaylistUri != null -> {
                    Log.d(
                        TAG,
                        "onStartCommand: Handling initial Playlist URI $initialPlaylistUri, autoPlay: $autoPlay"
                    )
                    customCommandHandler.processLoadPlaylistUri(initialPlaylistUri, autoPlay)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        saveCurrentQueueState()
        mediaSession.release()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class PlayerMediaSessionCallback : MediaSession.Callback {

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
                .add(COMMAND_LOAD_MORE_QUEUE)
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
            val handled = customCommandHandler.handleCustomCommand(customCommand, args)
            val resultCode = if (handled) {
                SessionResult.RESULT_SUCCESS
            } else {
                SessionError.ERROR_UNKNOWN
            }
            return Futures.immediateFuture(SessionResult(resultCode))
        }
    }
}

private class PlayerCustomCommandHandler(
    private val exoPlayer: ExoPlayer,
    private val musicRepository: MusicRepository,
    private val serviceScope: CoroutineScope,
) {
    private var isFetchingMore = false

    fun handleCustomCommand(
        customCommand: SessionCommand,
        args: Bundle,
    ): Boolean {
        return when (customCommand) {
            PlayerService.COMMAND_LOAD_STREAM_URI,
            PlayerService.COMMAND_LOAD_PLAYLIST_URI -> handleLoadCommand(customCommand, args)

            PlayerService.COMMAND_ENQUEUE_URI,
            PlayerService.COMMAND_ENQUEUE_NEXT_URI,
            PlayerService.COMMAND_ENQUEUE_PLAYLIST_URI,
            PlayerService.COMMAND_ENQUEUE_RADIO -> handleEnqueueCommand(customCommand, args)

            PlayerService.COMMAND_SEEK_TO_PERCENTAGE -> handleSeekCommand(args)

            PlayerService.COMMAND_LOAD_MORE_QUEUE -> {
                processLoadMoreQueue()
                true
            }

            else -> false
        }
    }

    private fun handleLoadCommand(customCommand: SessionCommand, args: Bundle): Boolean {
        val playWhenReady = args.getBoolean(PlayerService.KEY_PLAY_WHEN_READY, false)
        val uri = args.getString(PlayerService.KEY_URI)
        val playlistUri = args.getString(PlayerService.KEY_PLAYLIST_URI)
        return when (customCommand) {
            PlayerService.COMMAND_LOAD_STREAM_URI -> if (uri != null) {
                processLoadStreamUri(uri, playWhenReady)
                true
            } else false

            PlayerService.COMMAND_LOAD_PLAYLIST_URI -> if (playlistUri != null) {
                processLoadPlaylistUri(playlistUri, playWhenReady)
                true
            } else false

            else -> false
        }
    }

    private fun handleEnqueueCommand(customCommand: SessionCommand, args: Bundle): Boolean {
        val uri = args.getString(PlayerService.KEY_URI)
        val playlistUri = args.getString(PlayerService.KEY_PLAYLIST_URI)
        val targetUri = if (customCommand == PlayerService.COMMAND_ENQUEUE_PLAYLIST_URI) {
            playlistUri
        } else {
            uri
        }
        if (targetUri == null) return false
        processEnqueue(customCommand, targetUri)
        return true
    }

    private fun handleSeekCommand(args: Bundle): Boolean {
        val percentage = args.getFloat(PlayerService.KEY_PERCENTAGE, -1f)
        if (percentage != -1f) {
            processSeekToPercentage(percentage)
            return true
        }
        return false
    }

    fun processLoadMoreQueue() {
        if (isFetchingMore) return
        isFetchingMore = true
        serviceScope.launch {
            Log.d(PlayerService.TAG, "Fetching more items for queue...")
            val resultFlow = musicRepository.loadMorePlaylistItems()
            var addedCount = 0
            resultFlow.collect { result ->
                result.onSuccess { item ->
                    withContext(Dispatchers.Main) {
                        exoPlayer.addMediaItem(item.toMediaItem())
                        addedCount++
                    }
                }.onFailure { error ->
                    Log.e(PlayerService.TAG, "Error fetching more items: $error")
                }
            }
            if (addedCount > 0) {
                Log.d(PlayerService.TAG, "Added $addedCount more items to queue.")
            } else {
                Log.d(PlayerService.TAG, "No more items to fetch.")
            }
            isFetchingMore = false
        }
    }

    fun processLoadStreamUri(uri: String, playWhenReady: Boolean) {
        serviceScope.launch {
            val result = musicRepository.loadMediaUri(uri)
            result.onSuccess { item ->
                withContext(Dispatchers.Main) {
                    exoPlayer.setMediaItem(item.toMediaItem())
                    exoPlayer.prepare()
                    if (playWhenReady) exoPlayer.play()
                    Log.d(PlayerService.TAG, "Stream URI loaded. Play when ready: $playWhenReady")
                }
            }.onFailure { error ->
                Log.e(PlayerService.TAG, "Error loading stream URI $uri: $error")
            }
        }
    }

    fun processLoadPlaylistUri(playlistUri: String, playWhenReady: Boolean) {
        serviceScope.launch {
            exoPlayer.clearMediaItems()
            val playlistUriResult = musicRepository.loadPlaylistUri(playlistUri)
            val mediaItems = mutableListOf<MediaItem>()
            playlistUriResult.collect { result ->
                result.onSuccess { mediaItems.add(it.toMediaItem()) }
                    .onFailure { error ->
                        Log.e(
                            PlayerService.TAG,
                            "Error loading item from playlist $playlistUri: $error"
                        )
                    }
            }
            if (mediaItems.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    exoPlayer.setMediaItems(mediaItems)
                    exoPlayer.prepare()
                    if (playWhenReady) exoPlayer.play()
                    Log.d(PlayerService.TAG, "Playlist URI loaded. Play when ready: $playWhenReady")
                }
            } else {
                Log.w(
                    PlayerService.TAG,
                    "No media items successfully loaded from playlist URI $playlistUri"
                )
            }
        }
    }

    private fun processEnqueue(command: SessionCommand, uri: String) {
        serviceScope.launch {
            when (command) {
                PlayerService.COMMAND_ENQUEUE_URI,
                PlayerService.COMMAND_ENQUEUE_NEXT_URI -> {
                    val result = musicRepository.loadMediaUri(uri)
                    result.onSuccess { item ->
                        withContext(Dispatchers.Main) {
                            val index = if (command == PlayerService.COMMAND_ENQUEUE_NEXT_URI) {
                                exoPlayer.currentMediaItemIndex + 1
                            } else {
                                exoPlayer.mediaItemCount
                            }
                            exoPlayer.addMediaItem(index, item.toMediaItem())
                        }
                    }.onFailure { error ->
                        Log.e(PlayerService.TAG, "Error enqueueing URI $uri: $error")
                    }
                }

                PlayerService.COMMAND_ENQUEUE_PLAYLIST_URI -> {
                    val mediaItems = mutableListOf<MediaItem>()
                    musicRepository.loadPlaylistUri(uri).collect { result ->
                        result.onSuccess { mediaItems.add(it.toMediaItem()) }
                    }
                    if (mediaItems.isNotEmpty()) {
                        withContext(Dispatchers.Main) { exoPlayer.addMediaItems(mediaItems) }
                    }
                }

                PlayerService.COMMAND_ENQUEUE_RADIO -> {
                    musicRepository.loadAutoPlaylistUri(uri).collect { result ->
                        result.onSuccess { item ->
                            withContext(Dispatchers.Main) {
                                exoPlayer.addMediaItem(item.toMediaItem())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processSeekToPercentage(percentage: Float) {
        if (percentage !in 0f..1f) {
            Log.d(
                PlayerService.TAG,
                "processSeekToPercentage: incorrect percentage value: $percentage"
            )
            return
        }
        val duration = exoPlayer.duration
        if (duration == C.TIME_UNSET) {
            Log.d(
                PlayerService.TAG,
                "processSeekToPercentage: duration is not available for seek."
            )
        } else {
            val newPosition = (percentage * duration).toLong()
            exoPlayer.seekTo(newPosition)
            Log.d(
                PlayerService.TAG,
                "Media position changed to $newPosition ms (percentage: $percentage)"
            )
        }
    }
}
