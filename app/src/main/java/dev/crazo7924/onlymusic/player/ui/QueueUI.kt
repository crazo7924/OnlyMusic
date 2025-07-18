package dev.crazo7924.onlymusic.player.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import dev.crazo7924.onlymusic.MediaList
import dev.crazo7924.onlymusic.MediaListItem
import dev.crazo7924.onlymusic.MediaListType

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QueueUI(items: List<MediaListItem>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Queue", style = MaterialTheme.typography.displaySmall)
                }
            )
        },
    ) { innerPadding ->
        MediaList(
            contentPadding = innerPadding,
            mediaListItems = items,
            onItemClicked = {},
            onEnqueue = {},
            onEnqueueNext = {},
            mediaListType = MediaListType.QUEUE
        )
    }
}