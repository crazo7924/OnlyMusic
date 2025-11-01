package dev.crazo7924.onlymusic

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.crazo7924.onlymusic.player.PlaybackState
import dev.crazo7924.onlymusic.player.PlayerService
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.PlayerViewModelFactory
import dev.crazo7924.onlymusic.player.ui.PlayerUI
import dev.crazo7924.onlymusic.player.ui.QueueUI
import dev.crazo7924.onlymusic.player.ui.SearchUI
import dev.crazo7924.onlymusic.repository.NewPipeMusicRepository
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.SearchViewModelFactory
import dev.crazo7924.onlymusic.theme.OnlyMusicTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem


object Destination {
    const val QUEUE = "queue"
    const val SEARCH = "search"
    const val PLAYER = "player"
}

class MainActivity : ComponentActivity() {
    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            musicRepository = NewPipeMusicRepository(), minQueryLength = 2
        )
    }

    private val playerViewModel: PlayerViewModel by viewModels { PlayerViewModelFactory() }

    companion object {
        const val TAG = "MainActivity"
        const val UPDATE_DELAY = 500L

    }

    private var positionUpdateJob: Job? = null

    @VisibleForTesting
    var mediaControllerFuture: ListenableFuture<MediaController>? = null

    internal var mediaControllerBuilderFactory: (Context, SessionToken) -> MediaController.Builder =
        { context, sessionToken ->
            MediaController.Builder(context, sessionToken)
        }


    internal fun initializeMediaController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context.packageName, PlayerService::class.java.name))
        mediaControllerFuture = mediaControllerBuilderFactory(context, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            val mediaController = mediaControllerFuture?.get() ?: return@addListener
            Log.d(TAG, "MediaController connected.")

            // Initial state load
            playerViewModel.updateQueue(mediaController.currentTimeline.toMediaItems())
            playerViewModel.setCurrentMediaItem(mediaController.currentMediaItem)
            playerViewModel.setCurrentMediaItemIndex(mediaController.currentMediaItemIndex)
            playerViewModel.setPlaybackState(mediaController.isPlaying)


            positionUpdateJob?.cancel()
            positionUpdateJob = lifecycleScope.launch {
                while (isActive) {
                    playerViewModel.updatePosition(mediaController.currentPosition.coerceAtLeast(0L))
                    playerViewModel.updateDuration(mediaController.duration.coerceAtLeast(0L))
                    delay(UPDATE_DELAY)
                }
            }

            mediaController.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                        Log.d(
                            TAG,
                            "Listener: onMediaItemTransition to ${player.currentMediaItem?.mediaId}, index ${player.currentMediaItemIndex}"
                        )
                        playerViewModel.setQueueItemIndex(player.currentMediaItemIndex)
                        playerViewModel.setCurrentMediaItem(player.currentMediaItem)
                    }
                    if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                        Log.d(TAG, "Listener: onIsPlayingChanged to ${player.isPlaying}")
                        playerViewModel.setPlaybackState(player.isPlaying)
                    }
                    if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                        Log.d(TAG, "Listener: onPlaybackStateChanged to ${player.playbackState}")
                        when (player.playbackState) {
                            Player.STATE_BUFFERING -> playerViewModel.setLoading()
                            Player.STATE_ENDED -> playerViewModel.setStopped()
                            Player.STATE_IDLE -> playerViewModel.setStopped()
                            Player.STATE_READY -> {}
                        }
                    }
                    if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                        Log.d(TAG, "Listener: onTimelineChanged.")
                        val newQueue = mutableListOf<MediaItem>()
                        if (player.currentTimeline.windowCount > 0) {
                            for (i in 0 until player.currentTimeline.windowCount) {
                                val window = Timeline.Window()
                                player.currentTimeline.getWindow(i, window)
                                newQueue.add(window.mediaItem)
                            }
                        }
                        playerViewModel.updateQueue(newQueue)
                        // Potentially update current index again if timeline change implies it
                        if (player.currentMediaItemIndex != playerViewModel.uiState.value.currentMediaItemIndex) {
                            playerViewModel.setCurrentMediaItemIndex(player.currentMediaItemIndex)
                        }
                    }
                    if(events.contains(Player.EVENT_PLAYER_ERROR)) {
                        Log.d(TAG, "Listener: onPlayerError")
                        playerViewModel.setError(player.playerError?.message)
                        player.stop()
                    }
                    // Update duration frequently as it might change (e.g. live streams)
                    playerViewModel.updateDuration(player.duration.coerceAtLeast(0L))
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun releaseMediaController() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
            Log.d(TAG, "MediaController released.")
        }
        mediaControllerFuture = null
    }

    override fun onStart() {
        super.onStart()
        if (mediaControllerFuture == null || mediaControllerFuture?.isCancelled == true) {
            initializeMediaController(this@MainActivity)
        }
    }

    override fun onStop() {
        // Do not release controller here if playback should continue in background.
        // Release it in onStop if playback is tied to MainActivity's visibility.
        // For now, let's assume playback continues.
        // releaseMediaController()
        super.onStop()
    }

    override fun onDestroy() {
        releaseMediaController() // Release here for sure
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OnlyMusicTheme {
                val playerUiState by playerViewModel.uiState.collectAsState()
                val searchUiState by searchViewModel.uiState.collectAsState()
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        if(navBackStackEntry?.destination?.route != Destination.PLAYER) {
                            ListItem(
                                modifier = Modifier
                                    .clickable(
                                        onClick = {
                                            navController.navigate(Destination.PLAYER, navOptions = navOptions { launchSingleTop = true })
                                        }), headlineContent = {
                                    if (playerUiState.media == null) Text("Nothing is playing")
                                    else Text(
                                        text = playerUiState.media?.mediaMetadata?.title?.toString()
                                            ?: "Unknown Title"
                                    )
                                }, supportingContent = {
                                    if (playerUiState.media != null) {
                                        Text(
                                            text = playerUiState.media?.mediaMetadata?.artist?.toString()
                                                ?: "Unknown Artist"
                                        )
                                    }
                                }, leadingContent = {
                                    if (playerUiState.media != null) {
                                        AsyncImage(
                                            modifier = Modifier.size(48.dp),
                                            model = playerUiState.media?.mediaMetadata?.artworkUri,
                                            contentDescription = null
                                        )
                                    }
                                }, trailingContent = {
                                    if (playerUiState.media != null) {
                                        Row {
                                            IconButton(onClick = {
                                                navController.navigate(Destination.QUEUE, navOptions = navOptions { launchSingleTop = true })
                                            })
                                            {
                                                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
                                            }

                                            IconButton(onClick = {
                                                mediaControllerFuture?.get()?.prepare()
                                                if (playerUiState.playbackState == PlaybackState.PLAYING)
                                                    mediaControllerFuture?.get()?.pause()
                                                else
                                                    mediaControllerFuture?.get()?.play()
                                                Log.d(
                                                    TAG,
                                                    "Playback toggled. Current state: ${playerUiState.playbackState}"
                                                )
                                            }) {
                                                if (playerUiState.playbackState == PlaybackState.PLAYING)
                                                    Icon(Icons.Filled.Pause, contentDescription = null)
                                                else
                                                    Icon(
                                                        Icons.Filled.PlayArrow,
                                                        contentDescription = null
                                                    )

                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Destination.SEARCH,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        composable(Destination.SEARCH) {
                            SearchUI(
                                searchUiState = searchUiState,
                                onItemClicked = { item ->
                                    Log.d(
                                        TAG,
                                        "Item clicked from search results: ${item.mediaUri}"
                                    )
                                    val bundle = Bundle().apply {
                                        putString(
                                            PlayerService.KEY_URI,
                                            item.mediaUri
                                        )
                                        putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
                                    }
                                    val command = when (item.infoType) {
                                        InfoItem.InfoType.STREAM -> PlayerService.COMMAND_LOAD_STREAM_URI
                                        InfoItem.InfoType.PLAYLIST -> PlayerService.COMMAND_LOAD_PLAYLIST_URI
                                        else -> null
                                    }
                                    command?.let {
                                        mediaControllerFuture?.get()?.sendCustomCommand(it, bundle)
                                        Log.d(
                                            TAG,
                                            "Sent command ${it.customAction} with URI ${item.mediaUri}"
                                        )
                                    }
                                },
                                onEnqueue = { item ->
                                    Log.d(
                                        TAG,
                                        "Enqueuing from search results: ${item.mediaUri}"
                                    )
                                    if (item.infoType == InfoItem.InfoType.STREAM) {
                                        val bundle = Bundle().apply {
                                            putString(
                                                PlayerService.KEY_URI,
                                                item.mediaUri
                                            )
                                        }
                                        mediaControllerFuture?.get()?.sendCustomCommand(
                                            PlayerService.COMMAND_ENQUEUE_URI,
                                            bundle
                                        )
                                        Log.d(
                                            TAG,
                                            "Sent command ${PlayerService.COMMAND_ENQUEUE_URI.customAction} with URI ${item.mediaUri}"
                                        )
                                    }
                                },
                                onEnqueueNext = { item ->
                                    Log.d(
                                        TAG,
                                        "EnqueueNext (acting as Enqueue): ${item.mediaUri}"
                                    )
                                    if (item.infoType == InfoItem.InfoType.STREAM) {
                                        val bundle = Bundle().apply {
                                            putString(
                                                PlayerService.KEY_URI,
                                                item.mediaUri
                                            )
                                        }
                                        mediaControllerFuture?.get()?.sendCustomCommand(
                                            PlayerService.COMMAND_ENQUEUE_URI,
                                            bundle
                                        )
                                    }
                                },
                                onSearch = { searchViewModel.search() },
                                onSearchQueryUpdated = { searchViewModel.updateQueryFrom(it) },
                                onEnqueueRadio = { item ->
                                    Log.d(
                                        TAG,
                                        "Enqueuing Radio for item: ${item.mediaUri}"
                                    )

                                    val bundle = Bundle().apply {
                                        putString(
                                            PlayerService.KEY_URI,
                                            item.mediaUri
                                        )
                                    }
                                    mediaControllerFuture?.get()?.sendCustomCommand(
                                        PlayerService.COMMAND_START_RADIO,
                                        bundle
                                    )
                                    Log.d(
                                        TAG,
                                        "Sent command ${PlayerService.COMMAND_START_RADIO.customAction} with URI ${item.mediaUri}"
                                    )
                                },
                            )
                        }

                        composable(Destination.PLAYER) {
                            var positionForUpdate by remember { mutableFloatStateOf(0F) }
                            PlayerUI(
                                playerUiState = playerUiState,
                                onSeekTo = { position -> positionForUpdate = position },
                                onPlayPause = {
                                    mediaControllerFuture?.get()?.prepare()
                                    if (playerUiState.playbackState == PlaybackState.PLAYING)
                                        mediaControllerFuture?.get()?.pause()
                                    else
                                        mediaControllerFuture?.get()?.play()
                                    Log.d(
                                        TAG,
                                        "Playback toggled. Current state: ${playerUiState.playbackState}"
                                    )
                                },
                                onPlayNext = {
                                    mediaControllerFuture?.get()?.seekToNextMediaItem()
                                    Log.d(TAG, "SeekToNext triggered")
                                },
                                onPlayPrevious = {
                                    mediaControllerFuture?.get()?.seekToPreviousMediaItem()
                                    Log.d(TAG, "SeekToPrevious triggered")
                                },
                                onQueueIconClicked = {
                                    navController.navigate(Destination.QUEUE, navOptions = navOptions { launchSingleTop = true })
                                },
                                onPerformSeek = {
                                    Log.d(TAG, "Perform seek to percentage: $positionForUpdate")
                                    val bundle = Bundle().apply {
                                        putFloat(PlayerService.KEY_PERCENTAGE, positionForUpdate)
                                    }
                                    mediaControllerFuture?.get()?.sendCustomCommand(
                                        PlayerService.COMMAND_SEEK_TO_PERCENTAGE,
                                        bundle
                                    )
                                }
                            )
                        }

                        composable(Destination.QUEUE) {
                            QueueUI(
                                items = playerUiState.queue.map { it.toMediaListItem() }, // Adapt based on actual item type
                                onItemClicked = { index ->
                                    Log.d(TAG, "Queue item clicked: index $index")
                                    mediaControllerFuture?.get()?.seekToDefaultPosition(index)
                                    mediaControllerFuture?.get()
                                        ?.play() // Optional: start playing selected item
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Helper extension to convert Media3 [Timeline] to a list of [MediaItem]s for the ViewModel */
fun Timeline.toMediaItems(): List<MediaItem> {
    if (this.isEmpty) return emptyList()
    val items = mutableListOf<MediaItem>()
    for (i in 0 until this.windowCount) {
        val window = Timeline.Window()
        this.getWindow(i, window)
        items.add(window.mediaItem)
    }
    return items
}
