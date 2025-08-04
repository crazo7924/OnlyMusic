package dev.crazo7924.onlymusic.player.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import dev.crazo7924.onlymusic.MediaList
import dev.crazo7924.onlymusic.MediaListItem

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
        MediaList(
            modifier = Modifier.padding(padding),
            mediaItems = items,
            onItemClicked = { item, index ->
                onItemClicked(index)
            },
            onEnqueue = {},
            onEnqueueNext = {},
            onEnqueueRadio = {}
        )
    }
}