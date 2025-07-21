package dev.crazo7924.onlymusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.crazo7924.onlymusic.player.PlayerCmd
import dev.crazo7924.onlymusic.player.PlayerService
import dev.crazo7924.onlymusic.player.PlayerService.Companion.ACTION
import dev.crazo7924.onlymusic.player.PlayerService.Companion.ENQUEUE_URI
import dev.crazo7924.onlymusic.player.PlayerService.Companion.PLAYLIST_URI
import dev.crazo7924.onlymusic.player.PlayerService.Companion.POSITION
import dev.crazo7924.onlymusic.player.PlayerService.Companion.STREAM_URI
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.PlayerViewModelFactory
import dev.crazo7924.onlymusic.player.ui.PlayerUI
import dev.crazo7924.onlymusic.player.ui.QueueUI
import dev.crazo7924.onlymusic.player.ui.SearchUI
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.SearchViewModelFactory
import dev.crazo7924.onlymusic.search.data.NewPipeSearchRepository
import dev.crazo7924.onlymusic.theme.OnlyMusicTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem


class MainActivity : ComponentActivity() {
    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            searchRepository = NewPipeSearchRepository(), minQueryLength = 2
        )
    }

    private val playerViewModel: PlayerViewModel by viewModels { PlayerViewModelFactory() }

    companion object {
        const val TAG = "MainActivity"
        const val UPDATE_DELAY = 500L

    }

    /**
     * A job which updates the playback position via [playerViewModel]
     * from the [MediaController] periodically.
     *
     * To avoid persistence, it is made nullable.
     */
    private var positionUpdateJob: Job? = null

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    fun initializeMediaController(context: Context) {
        val sessionToken = SessionToken(
            context, ComponentName(context, PlayerService::class.java)
        )

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({

            // When the UI appears,
            // 1. set the current media item to load in UI
            playerViewModel.setMediaItem(mediaControllerFuture!!.get().currentMediaItem)
            playerViewModel.restorePlayPauseToggle(mediaControllerFuture!!.get().isPlaying)
            // 2. assign a new job for position UI updates
            positionUpdateJob?.cancel()
            positionUpdateJob = lifecycleScope.launch {
                while (isActive) {
                    playerViewModel.updatePosition(mediaControllerFuture!!.get().currentPosition)
                    delay(UPDATE_DELAY)
                }
            }
            mediaControllerFuture!!.get().addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    playerViewModel.setMediaItem(mediaItem)
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    /*
                     * Notice that `isPlaying` is inverted before passing the same.
                     * This is because it _already_ has the updated value.
                     *
                     * The usage of `wasPlaying` as the formal parameter name gives more clarity
                     */
                    playerViewModel.onPlayPauseToggled(!isPlaying)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {

                        Player.STATE_BUFFERING,
                        Player.STATE_IDLE,
                        Player.STATE_READY,
                            -> playerViewModel.setLoading()

                        Player.STATE_ENDED -> {
                            /* no-op */
                        }
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    /**
     * Called when UI is out of sight i.e. goes into the background
     */
    fun releaseMediaController() {
        // Cancel the SeekBar position update job here for convenience
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        MediaController.releaseFuture(mediaControllerFuture!!)
    }

    override fun onStart() {
        super.onStart()
        if (mediaControllerFuture?.isDone == true) return
        if (mediaControllerFuture?.isCancelled == true) return
        initializeMediaController(this@MainActivity)
    }

    override fun onStop() {
        super.onStop()
        releaseMediaController()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OnlyMusicTheme {
                val playerUiState = playerViewModel.uiState.collectAsState()
                val searchUiState = searchViewModel.uiState.collectAsState()

                // TODO: implement this hacky UI in a better way

                val pagerState = rememberPagerState(initialPage = 0) { 3 }
                val pagerCoroutineScope = rememberCoroutineScope()
                VerticalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            SearchUI(
                                searchViewModel = searchViewModel,
                                searchUiState = searchUiState.value,
                                playerUiState = playerUiState.value,
                                onItemClicked = { item ->
                                    Log.d(
                                        TAG, "Item clicked from search results: ${item.mediaUri}"
                                    )
                                    when (item.infoType) {
                                        InfoItem.InfoType.STREAM -> {
                                            val intent =
                                                createPlayerServiceIntent(this@MainActivity)
                                            intent.putExtra(STREAM_URI, item.mediaUri)
                                            startService(intent)
                                        }

                                        InfoItem.InfoType.PLAYLIST -> {
                                            val intent =
                                                createPlayerServiceIntent(this@MainActivity)
                                            intent.putExtra(PLAYLIST_URI, item.mediaUri)
                                            startService(intent)
                                        }

                                        else -> {/* no-op */
                                        }
                                    }

                                },
                                onEnqueue = { item ->
                                    Log.d(TAG, "Enqueuing from search results: ${item.mediaUri}")
                                    if (item.infoType == InfoItem.InfoType.STREAM) {
                                        val intent = createPlayerServiceIntent(
                                            this@MainActivity,
                                            PlayerCmd.ENQUEUE
                                        )
                                        intent.putExtra(ENQUEUE_URI, item.mediaUri)
                                        startService(intent)
                                    }
                                },
                                onEnqueueNext = { item ->
                                    Log.d(TAG, "Enqueuing from search results: ${item.mediaUri}")
                                    if (item.infoType == InfoItem.InfoType.STREAM) {
                                        val intent = createPlayerServiceIntent(
                                            this@MainActivity,
                                            PlayerCmd.ENQUEUE_NEXT
                                        )
                                        intent.putExtra(ENQUEUE_URI, item.mediaUri)
                                        startService(intent)
                                    }
                                },
                                onPlayerPreviewClicked = {
                                    pagerCoroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                }
                            )
                        }

                        1 -> {
                            Scaffold { innerPadding ->
                                PlayerUI(
                                    modifier = Modifier
                                        .padding(innerPadding),
                                    playerUiState = playerUiState.value,
                                    onSeekTo = { position ->
                                        val intent =
                                            createPlayerServiceIntent(
                                                context = this@MainActivity,
                                                playerCmd = PlayerCmd.SEEK_TO
                                            )
                                        intent.putExtra(
                                            POSITION, position.toLong().toString()
                                        )
                                        startService(intent)
                                    },
                                    onPlayPause = {
                                        val intent =
                                            createPlayerServiceIntent(
                                                context = this@MainActivity,
                                                playerCmd = PlayerCmd.PLAY_PAUSE
                                            )
                                        startService(intent)
                                    },
                                    onPlayNext = {
                                        val intent =
                                            createPlayerServiceIntent(
                                                context = this@MainActivity,
                                                playerCmd = PlayerCmd.NEXT
                                            )
                                        startService(intent)
                                    },
                                    onPlayPrevious = {
                                        val intent =
                                            createPlayerServiceIntent(
                                                context = this@MainActivity,
                                                playerCmd = PlayerCmd.PREV
                                            )
                                        startService(intent)
                                    })
                            }
                        }

                        2 -> {
                            QueueUI(mediaControllerFuture?.get()?.currentTimeline?.toMediaListItem()!!)
                        }
                    }
                }
                val backPressedCallback = object : OnBackPressedCallback(enabled = true) {
                    override fun handleOnBackPressed() {
                        if (pagerState.currentPage == 0) finish()
                        else pagerCoroutineScope.launch {
                            pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                        }
                    }
                }

                onBackPressedDispatcher.addCallback(
                    owner = this@MainActivity,
                    onBackPressedCallback = backPressedCallback
                )
            }
        }
    }

    fun createPlayerServiceIntent(context: Context, playerCmd: PlayerCmd? = null): Intent {
        val intent = Intent(
            context, PlayerService::class.java
        )
        playerCmd?.let { intent.putExtra(ACTION, it.ordinal) }
        return intent
    }
}

