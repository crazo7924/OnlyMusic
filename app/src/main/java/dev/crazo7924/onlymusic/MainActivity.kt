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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil3.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dev.crazo7924.onlymusic.player.PlayerCmd
import dev.crazo7924.onlymusic.player.PlayerService
import dev.crazo7924.onlymusic.player.PlayerService.Companion.ACTION_ARG
import dev.crazo7924.onlymusic.player.PlayerService.Companion.PLAYLIST_URI_ARG
import dev.crazo7924.onlymusic.player.PlayerService.Companion.POSITION_ARG
import dev.crazo7924.onlymusic.player.PlayerService.Companion.STREAM_URI_ARG
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.PlayerViewModelFactory
import dev.crazo7924.onlymusic.player.ui.PlayerUI
import dev.crazo7924.onlymusic.search.SearchState
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.SearchViewModelFactory
import dev.crazo7924.onlymusic.search.data.NewPipeSearchRepository
import dev.crazo7924.onlymusic.search.ui.TopSearchBar
import dev.crazo7924.onlymusic.theme.OnlyMusicTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    fun initializeMediaController(context: Context) {
        val sessionToken = SessionToken(
            context, ComponentName(context, PlayerService::class.java)
        )

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture.addListener({

            // When the UI appears,
            // 1. set the current media item to load in UI
            playerViewModel.setMediaItem(mediaControllerFuture.get().currentMediaItem)
            playerViewModel.restorePlayPauseToggle(mediaControllerFuture.get().isPlaying)
            // 2. assign a new job for position UI updates
            positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    playerViewModel.updatePosition(mediaControllerFuture.get().currentPosition)
                    delay(UPDATE_DELAY)
                }
            }
            mediaControllerFuture.get().addListener(object : Player.Listener {
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

                        Player.STATE_ENDED -> {/* no-op */
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
        MediaController.releaseFuture(mediaControllerFuture)
    }

    override fun onRestart() {
        super.onRestart()
        initializeMediaController(this@MainActivity)
    }

    override fun onStop() {
        super.onStop()
        releaseMediaController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OnlyMusicTheme {
                val playerUiState = playerViewModel.uiState.collectAsState()
                val searchUiState = searchViewModel.uiState.collectAsState()

                // TODO: implement this hacky UI in a better way

                val pagerState = rememberPagerState(initialPage = 0) { 2 }
                val scrollScope = rememberCoroutineScope()
                VerticalPager(state = pagerState) { page ->
                    if (page == 0) Scaffold(topBar = {
                        TopSearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding(),
                            query = searchUiState.value.query,
                            onQueryChange = { searchViewModel.updateQueryFrom(it) },
                            onSearch = { searchViewModel.search() },
                            placeholder = stringResource(R.string.app_name),
                            iconDescription = stringResource(R.string.search_bar_indicator_icon_description),
                        )
                    }, bottomBar = {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ListItem(headlineContent = {
                                if (playerUiState.value.media == null) Text("Nothing is playing")
                                else Text(
                                    text = playerUiState.value.media!!.mediaMetadata.title?.toString()
                                        ?: "Unknown Title"
                                )
                            }, supportingContent = {
                                if (playerUiState.value.media != null) Text(
                                    text = playerUiState.value.media!!.mediaMetadata.artist?.toString()
                                        ?: "Unknown Artist"
                                )
                            }, leadingContent = {
                                AsyncImage(
                                    modifier = Modifier.size(48.dp),
                                    model = playerUiState.value.media?.mediaMetadata?.artworkUri,
                                    contentDescription = null
                                )
                            })
                        }
                    }) { innerPadding ->


                        when (searchUiState.value.searchState) {
                            SearchState.INITIAL -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Text(
                                    text = stringResource(R.string.search_help_text),
                                    textAlign = TextAlign.Center
                                )
                            }

                            SearchState.SEARCHING -> LazyColumn(
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            ) {
                                items(count = 8) {
                                    ListItem(
                                        headlineContent = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(72.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .shimmerLoading()
                                            )
                                        })
                                }
                            }


                            SearchState.SUCCESS -> MediaList(
                                modifier = Modifier.padding(innerPadding),
                                mediaListItems = searchUiState.value.suggestions,
                                mediaListType = MediaListType.SUGGESTIONS,
                                onItemClicked = { item, _ ->
                                    Log.d(
                                        TAG, "Item clicked from search results: ${item.mediaUri}"
                                    )
                                    when (item.infoType) {
                                        InfoItem.InfoType.STREAM -> {
                                            val intent =
                                                createPlayerServiceIntent(this@MainActivity)
                                            intent.putExtra(STREAM_URI_ARG, item.mediaUri)
                                            startService(intent)
                                        }

                                        InfoItem.InfoType.PLAYLIST -> {
                                            val intent =
                                                createPlayerServiceIntent(this@MainActivity)
                                            intent.putExtra(PLAYLIST_URI_ARG, item.mediaUri)
                                            startService(intent)
                                        }

                                        else -> {/* no-op */
                                        }
                                    }
                                })

                            SearchState.ERROR -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.something_went_wrong),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (page == 1) {
                        Scaffold { innerPadding ->
                            PlayerUI(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .statusBarsPadding(),
                                uiState = playerUiState.value,
                                onSeekTo = { position ->
                                    val intent =
                                        createPlayerServiceIntent(
                                            context = this@MainActivity,
                                            playerCmd = PlayerCmd.SEEK_TO
                                        )
                                    intent.putExtra(
                                        POSITION_ARG, position.toLong().toString()
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
                                },
                            )
                        }
                    }
                }
                val backPressedCallback = object : OnBackPressedCallback(enabled = true) {
                    override fun handleOnBackPressed() {
                        if (pagerState.currentPage == 0) finish()
                        else scrollScope.launch {
                            pagerState.animateScrollToPage(page = 0)
                        }
                    }
                }

                onBackPressedDispatcher.addCallback(
                    owner = this@MainActivity,
                    onBackPressedCallback = backPressedCallback
                )
            }
        }
        initializeMediaController(this@MainActivity)
    }

    fun createPlayerServiceIntent(context: Context, playerCmd: PlayerCmd? = null): Intent {
        val intent = Intent(
            context, PlayerService::class.java
        )
        playerCmd?.let { intent.putExtra(ACTION_ARG, it.ordinal) }
        return intent
    }
}
