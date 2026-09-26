/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.ui.main

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.MediaControllerManager
import dev.crazo7924.onlymusic.R
import dev.crazo7924.onlymusic.core.toMediaListItem
import dev.crazo7924.onlymusic.features.player.PlaybackState
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import dev.crazo7924.onlymusic.features.player.ui.PlayerUI
import dev.crazo7924.onlymusic.features.player.ui.QueueUI
import dev.crazo7924.onlymusic.features.search.SearchUiState
import dev.crazo7924.onlymusic.features.search.SearchViewModel
import dev.crazo7924.onlymusic.features.search.ui.SearchUI
import dev.crazo7924.onlymusic.service.PlayerService
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem
import dev.crazo7924.onlymusic.core.R as commonR

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionNaming")
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel,
    mediaControllerManager: MediaControllerManager,
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
            SheetContent(
                isExpanded = isExpanded,
                playerUiState = playerUiState,
                mediaControllerManager = mediaControllerManager,
                pagerState = pagerState,
                onExpand = { scope.launch { scaffoldState.bottomSheetState.expand() } },
                onCollapse = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } },
                onQueueClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                        pagerState.animateScrollToPage(page = 1)
                    }
                }
            )
        }
    ) { _ ->
        MainSearchContent(
            searchUiState = searchUiState,
            searchViewModel = searchViewModel,
            mediaControllerManager = mediaControllerManager
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun SheetContent(
    isExpanded: Boolean,
    playerUiState: PlayerUiState,
    mediaControllerManager: MediaControllerManager,
    pagerState: PagerState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onQueueClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = !isExpanded,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 400),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 400),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = tween(durationMillis = 400))
    ) {
        MiniPlayerBar(
            playerUiState = playerUiState,
            onExpand = onExpand,
            onQueueClick = onQueueClick,
            onPlayPauseClick = { togglePlayback(mediaControllerManager, playerUiState) }
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 400),
            initialOffsetY = { -it }
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 400),
            targetOffsetY = { -it }
        ) + fadeOut(animationSpec = tween(durationMillis = 400))
    ) {
        ExpandedPlayerSheet(
            playerUiState = playerUiState,
            mediaControllerManager = mediaControllerManager,
            pagerState = pagerState,
            onCollapse = onCollapse
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun MiniPlayerBar(
    playerUiState: PlayerUiState,
    onExpand: () -> Unit,
    onQueueClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
) {
    val media = playerUiState.media
    val isPlaying = playerUiState.playbackState == PlaybackState.PLAYING

    ListItem(
        modifier = Modifier
            .clickable(onClick = onExpand)
            .animateContentSize(),
        headlineContent = {
            if (media == null) {
                Text(stringResource(R.string.nothing_is_playing))
            } else {
                Text(
                    text = media.mediaMetadata.title?.toString()
                        ?: stringResource(commonR.string.song_unknown_title)
                )
            }
        },
        supportingContent = {
            if (media != null) {
                Text(
                    text = media.mediaMetadata.artist?.toString()
                        ?: stringResource(commonR.string.song_unknown_artist)
                )
            }
        },
        leadingContent = {
            if (media != null) {
                AsyncImage(
                    modifier = Modifier.size(48.dp),
                    model = media.mediaMetadata.artworkUri,
                    contentDescription = null
                )
            }
        },
        trailingContent = {
            if (media != null) {
                MiniPlayerTrailingActions(
                    isPlaying = isPlaying,
                    onQueueClick = onQueueClick,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    )
}

@Suppress("FunctionNaming")
@Composable
private fun MiniPlayerTrailingActions(
    isPlaying: Boolean,
    onQueueClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
) {
    Row {
        IconButton(onClick = onQueueClick) {
            Icon(
                Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = stringResource(
                    commonR.string.queue_icon_description
                )
            )
        }

        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) commonR.string.pause_icon_description
                    else commonR.string.play_icon_description
                )
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ExpandedPlayerSheet(
    playerUiState: PlayerUiState,
    mediaControllerManager: MediaControllerManager,
    pagerState: PagerState,
    onCollapse: () -> Unit,
) {
    VerticalPager(
        modifier = Modifier.animateContentSize(),
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> PlayerPage(
                playerUiState = playerUiState,
                mediaControllerManager = mediaControllerManager,
                pagerState = pagerState,
                onCollapse = onCollapse
            )

            1 -> QueueUI(
                items = playerUiState.queue.map { it.toMediaListItem() },
                currentIndex = playerUiState.currentMediaItemIndex,
                onItemClicked = { index ->
                    Log.d(MainActivity.TAG, "Queue item clicked: index $index")
                    val controller = mediaControllerManager.getController()
                    controller?.seekToDefaultPosition(index)
                    controller?.play()
                },
                onLoadMore = {
                    val controller = mediaControllerManager.getController()
                    controller?.sendCustomCommand(
                        PlayerService.COMMAND_LOAD_MORE_QUEUE,
                        Bundle.EMPTY
                    )
                }
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun PlayerPage(
    playerUiState: PlayerUiState,
    mediaControllerManager: MediaControllerManager,
    pagerState: PagerState,
    onCollapse: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    PlayerUI(
        playerUiState = playerUiState,
        onSeekTo = { position ->
            val controller = mediaControllerManager.getController()
            val percentage = if (position > 0) {
                controller?.duration?.let { position / it } ?: 0F
            } else 0F
            Log.d(MainActivity.TAG, "Perform seek to percentage: $percentage")
            val bundle = Bundle().apply {
                putFloat(PlayerService.KEY_PERCENTAGE, percentage)
            }
            controller?.sendCustomCommand(
                PlayerService.COMMAND_SEEK_TO_PERCENTAGE,
                bundle
            )
        },
        onPlayPause = { togglePlayback(mediaControllerManager, playerUiState) },
        onPlayNext = {
            mediaControllerManager.getController()?.seekToNextMediaItem()
            Log.d(MainActivity.TAG, "SeekToNext triggered")
        },
        onPlayPrevious = {
            mediaControllerManager.getController()?.seekToPreviousMediaItem()
            Log.d(MainActivity.TAG, "SeekToPrevious triggered")
        },
        onQueueIconClicked = {
            scope.launch { pagerState.animateScrollToPage(page = 1) }
        },
        onRadioIconClicked = {
            scope.launch {
                playerUiState.media?.mediaId?.let { mediaId ->
                    val bundle = Bundle().apply {
                        putString(
                            PlayerService.KEY_URI,
                            "https://music.youtube.com/watch?v=$mediaId"
                        )
                    }
                    mediaControllerManager.getController()?.sendCustomCommand(
                        PlayerService.COMMAND_ENQUEUE_RADIO,
                        bundle
                    )
                    Log.d(MainActivity.TAG, "Enqueue radio triggered")
                }
            }
        },
        onCollapse = onCollapse
    )
}

@Suppress("FunctionNaming")
@Composable
private fun MainSearchContent(
    searchUiState: SearchUiState,
    searchViewModel: SearchViewModel,
    mediaControllerManager: MediaControllerManager,
) {
    SearchUI(
        searchUiState = searchUiState,
        onItemClicked = { item ->
            Log.d(MainActivity.TAG, "Item clicked from search results: ${item.mediaUri}")
            val bundle = Bundle().apply {
                putString(PlayerService.KEY_URI, item.mediaUri)
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
                    MainActivity.TAG,
                    "Sent command ${it.customAction} with URI ${item.mediaUri}"
                )
            }
        },
        onEnqueue = { item ->
            Log.d(MainActivity.TAG, "Enqueuing from search results: ${item.mediaUri}")
            if (item.infoType == InfoItem.InfoType.STREAM) {
                val bundle = Bundle().apply {
                    putString(PlayerService.KEY_URI, item.mediaUri)
                }
                mediaControllerManager.getController()?.sendCustomCommand(
                    PlayerService.COMMAND_ENQUEUE_URI, bundle
                )
            }
        },
        onEnqueueNext = { item ->
            Log.d(MainActivity.TAG, "EnqueueNext: ${item.mediaUri}")
            if (item.infoType == InfoItem.InfoType.STREAM) {
                val bundle = Bundle().apply {
                    putString(PlayerService.KEY_URI, item.mediaUri)
                }
                mediaControllerManager.getController()?.sendCustomCommand(
                    PlayerService.COMMAND_ENQUEUE_NEXT_URI, bundle
                )
            }
        },
        onSearch = { searchViewModel.search() },
        onSearchQueryUpdated = { searchViewModel.updateQueryFrom(it) },
        onQuerySelected = { searchViewModel.onQuerySelected(it) },
        onEnqueueRadio = { item ->
            Log.d(MainActivity.TAG, "Enqueuing Radio for item: ${item.mediaUri}")
            val bundle = Bundle().apply {
                putString(PlayerService.KEY_URI, item.mediaUri)
            }
            mediaControllerManager.getController()?.sendCustomCommand(
                PlayerService.COMMAND_ENQUEUE_RADIO, bundle
            )
        },
        onDeleteRecentQuery = { searchViewModel.deleteRecentQuery(it) }
    )
}

private fun togglePlayback(
    mediaControllerManager: MediaControllerManager,
    playerUiState: PlayerUiState,
) {
    val controller = mediaControllerManager.getController()
    controller?.prepare()
    if (playerUiState.playbackState == PlaybackState.PLAYING) {
        controller?.pause()
    } else {
        controller?.play()
    }
    Log.d(
        MainActivity.TAG,
        "Playback toggled. Current state: ${playerUiState.playbackState}"
    )
}
