/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.player.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.core.ui.components.iconForInfoType
import org.schabi.newpipe.extractor.InfoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueUI(items: List<MediaListItem>, onItemClicked: (Int) -> Unit) {
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
            onItemClicked = { index ->
                onItemClicked(index)
            },
        )
    }
}

@Composable
fun QueueList(
    modifier: Modifier = Modifier,
    mediaItems: List<MediaListItem>,
    onItemClicked: (Int) -> Unit,
) {

    LazyColumn(modifier = modifier) {
        items(count = mediaItems.size) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable(onClick = { onItemClicked(index) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val intrinsicSize = with(LocalDensity.current) {
                    Size(48.dp.toPx(), 48.dp.toPx())
                }

                val icon = iconForInfoType(mediaItems[index].infoType, intrinsicSize)

                AsyncImage(
                    modifier = Modifier.size(64.dp),
                    model = ImageRequest.Builder(
                        LocalContext.current
                    ).crossfade(true).data(mediaItems[index].thumbnailUri).build(),
                    contentDescription = null,
                    error = icon,
                    placeholder = icon,
                    fallback = icon,
                    clipToBounds = true
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = mediaItems[index].title ?: "Unknown Title",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = mediaItems[index].artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QueuePreview() {
    QueueUI(
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