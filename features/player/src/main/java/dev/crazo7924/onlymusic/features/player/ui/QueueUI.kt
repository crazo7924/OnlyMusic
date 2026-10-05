/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.player.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.core.ui.components.MediaListItemRow
import org.schabi.newpipe.extractor.InfoItem

@Suppress("FunctionNaming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueUI(items: List<MediaListItem>, currentIndex: Int, onItemClicked: (Int) -> Unit, onLoadMore: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Queue", style = MaterialTheme.typography.displaySmall)
                })
        },
    ) { padding ->
        QueueList(
            modifier = Modifier.padding(padding),
            mediaItems = items,
            currentIndex = currentIndex,
            onItemClicked = { index ->
                onItemClicked(index)
            },
            onLoadMore = onLoadMore
        )
    }
}

@Suppress("LongMethod", "FunctionNaming")
@Composable
fun QueueList(
    modifier: Modifier = Modifier,
    mediaItems: List<MediaListItem>,
    currentIndex: Int,
    onItemClicked: (Int) -> Unit,
    onLoadMore: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleItemIndex != null && lastVisibleItemIndex >= totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    LazyColumn(modifier = modifier.testTag("queue_list"), state = listState) {
        items(count = mediaItems.size) { index ->
            val isPlaying = index == currentIndex
            val isPlayed = index < currentIndex

            MediaListItemRow(
                mediaListItem = mediaItems[index],
                imageSize = if (isPlaying) 80.dp else 64.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .animateContentSize()
                    .clickable(onClick = { onItemClicked(index) })
                    .then(
                        if (isPlayed) {
                            Modifier.drawWithContent {
                                drawContent()
                                drawRect(Color.Gray.copy(alpha = 0.4f))
                            }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

// no translation of strings required for preview with dummy data
@Suppress("FunctionNaming")
@Preview(showBackground = true)
@Composable
private fun QueuePreview() {
    QueueUI(
        currentIndex = 1,
        items = listOf(
            MediaListItem(
                title = "Song 1",
                artist = "Artist 1",
                infoType = InfoItem.InfoType.STREAM,
                thumbnailUri = null,
                mediaUri = null,
                id = "1"
            ), MediaListItem(
                title = "Playlist 1",
                artist = "Artist 2",
                infoType = InfoItem.InfoType.PLAYLIST,
                thumbnailUri = null,
                mediaUri = null,
                id = "2"
            ), MediaListItem(
                title = "Channel 1",
                artist = "Artist 3",
                infoType = InfoItem.InfoType.CHANNEL,
                thumbnailUri = null,
                mediaUri = null,
                id = "3"
            )
        ),
        onItemClicked = {},
    )
}
