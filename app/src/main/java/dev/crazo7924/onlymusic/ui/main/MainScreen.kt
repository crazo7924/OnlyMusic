package dev.crazo7924.onlymusic.ui.main

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.player.MediaControllerManager
import dev.crazo7924.onlymusic.player.PlaybackState
import dev.crazo7924.onlymusic.player.PlayerService
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.ui.PlayerUI
import dev.crazo7924.onlymusic.player.ui.QueueUI
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.ui.SearchUI
import dev.crazo7924.onlymusic.ui.components.toMediaListItem
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel,
    mediaControllerManager: MediaControllerManager
) {
    val playerUiState by playerViewModel.uiState.collectAsState()
    val searchUiState by searchViewModel.uiState.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    BottomSheetScaffold(
        sheetPeekHeight = 80.dp,
        scaffoldState = scaffoldState,
        sheetShape = RectangleShape,
        sheetDragHandle = null,
        sheetContent = {
            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                ListItem(
                    modifier = Modifier.clickable(
                        onClick = {
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
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
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                        pagerState.animateScrollToPage(page = 1)
                                    }


                                }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null
                                    )
                                }

                                IconButton(onClick = {
                                    val controller = mediaControllerManager.getController()
                                    controller?.prepare()
                                    if (playerUiState.playbackState == PlaybackState.PLAYING) controller?.pause()
                                    else controller?.play()
                                    Log.d(
                                        MainActivity.TAG,
                                        "Playback toggled. Current state: ${playerUiState.playbackState}"
                                    )
                                }) {
                                    if (playerUiState.playbackState == PlaybackState.PLAYING) Icon(
                                        Icons.Filled.Pause, contentDescription = null
                                    )
                                    else Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )

                                }
                            }
                        }
                    })
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                VerticalPager(
                    state = pagerState,
                ) { page ->
                    when (page) {
                        0 -> {
                            PlayerUI(
                                playerUiState = playerUiState,
                                onSeekTo = { position ->
                                    val controller = mediaControllerManager.getController()
                                    val percentage = if (position > 0) {
                                        controller?.duration?.let { position / it }
                                            ?: 0F
                                    } else 0F
                                    Log.d(
                                        MainActivity.TAG, "Perform seek to percentage: $percentage"
                                    )
                                    val bundle = Bundle().apply {
                                        putFloat(
                                            PlayerService.KEY_PERCENTAGE, percentage
                                        )
                                    }
                                    controller?.sendCustomCommand(
                                        PlayerService.COMMAND_SEEK_TO_PERCENTAGE, bundle
                                    )
                                },
                                onPlayPause = {
                                    val controller = mediaControllerManager.getController()
                                    controller?.prepare()
                                    if (playerUiState.playbackState == PlaybackState.PLAYING) controller?.pause()
                                    else controller?.play()
                                    Log.d(
                                        MainActivity.TAG,
                                        "Playback toggled. Current state: ${playerUiState.playbackState}"
                                    )
                                },
                                onPlayNext = {
                                    mediaControllerManager.getController()?.seekToNextMediaItem()
                                    Log.d(MainActivity.TAG, "SeekToNext triggered")
                                },
                                onPlayPrevious = {
                                    mediaControllerManager.getController()
                                        ?.seekToPreviousMediaItem()
                                    Log.d(MainActivity.TAG, "SeekToPrevious triggered")
                                },
                                onQueueIconClicked = {
                                    scope.launch { pagerState.animateScrollToPage(page = 1) }
                                },
                                onCollapse = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                })
                        }


                        1 -> {
                            QueueUI(
                                items = playerUiState.queue.map { it.toMediaListItem() }, // Adapt based on actual item type
                                onItemClicked = { index ->
                                    Log.d(MainActivity.TAG, "Queue item clicked: index $index")
                                    val controller = mediaControllerManager.getController()
                                    controller?.seekToDefaultPosition(index)
                                    controller?.play() // Optional: start playing selected item
                                })
                        }
                    }
                }
            }

        }) { _ ->

        SearchUI(
            searchUiState = searchUiState,
            onItemClicked = { item ->
                Log.d(
                    MainActivity.TAG, "Item clicked from search results: ${item.mediaUri}"
                )
                val bundle = Bundle().apply {
                    putString(
                        PlayerService.KEY_URI, item.mediaUri
                    )
                    putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
                }
                val command = when (item.infoType) {
                    InfoItem.InfoType.STREAM -> PlayerService.COMMAND_LOAD_STREAM_URI
                    InfoItem.InfoType.PLAYLIST -> PlayerService.COMMAND_LOAD_PLAYLIST_URI
                    else -> null
                }
                command?.let {
                    mediaControllerManager.getController()?.sendCustomCommand(it, bundle)
                    Log.d(
                        MainActivity.TAG, "Sent command ${it.customAction} with URI ${item.mediaUri}"
                    )
                }
            },
            onEnqueue = { item ->
                Log.d(
                    MainActivity.TAG, "Enqueuing from search results: ${item.mediaUri}"
                )
                if (item.infoType == InfoItem.InfoType.STREAM) {
                    val bundle = Bundle().apply {
                        putString(
                            PlayerService.KEY_URI, item.mediaUri
                        )
                    }
                    mediaControllerManager.getController()?.sendCustomCommand(
                        PlayerService.COMMAND_ENQUEUE_URI, bundle
                    )
                    Log.d(
                        MainActivity.TAG,
                        "Sent command ${PlayerService.COMMAND_ENQUEUE_URI.customAction} with URI ${item.mediaUri}"
                    )
                }
            },
            onEnqueueNext = { item ->
                Log.d(
                    MainActivity.TAG, "EnqueueNext: ${item.mediaUri}"
                )
                if (item.infoType == InfoItem.InfoType.STREAM) {
                    val bundle = Bundle().apply {
                        putString(
                            PlayerService.KEY_URI, item.mediaUri
                        )
                    }
                    mediaControllerManager.getController()?.sendCustomCommand(
                        PlayerService.COMMAND_ENQUEUE_NEXT_URI, bundle
                    )
                }
            },
            onSearch = { searchViewModel.search() },
            onSearchQueryUpdated = { searchViewModel.updateQueryFrom(it) },
            onEnqueueRadio = { item ->
                Log.d(
                    MainActivity.TAG, "Enqueuing Radio for item: ${item.mediaUri}"
                )

                val bundle = Bundle().apply {
                    putString(
                        PlayerService.KEY_URI, item.mediaUri
                    )
                }
                mediaControllerManager.getController()?.sendCustomCommand(
                    PlayerService.COMMAND_ENQUEUE_RADIO, bundle
                )
                Log.d(
                    MainActivity.TAG,
                    "Sent command ${PlayerService.COMMAND_ENQUEUE_RADIO.customAction} with URI ${item.mediaUri}"
                )
            },
        )
    }
}
