package dev.crazo7924.onlymusic

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.schabi.newpipe.extractor.InfoItem

@Composable
fun MediaList(
    modifier: Modifier = Modifier,
    mediaItems: List<MediaListItem>,
    onItemClicked: (MediaListItem, Int) -> Unit,
    onEnqueue: (MediaListItem) -> Unit,
    onEnqueueNext: (MediaListItem) -> Unit,
    onEnqueueRadio: (MediaListItem) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(count = mediaItems.size) { index ->
            var menuVisible by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp, max = 64.dp)
                        .animateContentSize()
                        .combinedClickable(
                            onClick = { onItemClicked(mediaItems[index], index) },
                            onLongClick = {
                                menuVisible = true
                            },
                        )
                ) {
                    val icon = forwardingPainter(
                        rememberVectorPainter(
                            when (mediaItems[index].infoType) {
                                // Song
                                InfoItem.InfoType.STREAM -> Icons.Rounded.MusicNote
                                // Album / Playlist
                                InfoItem.InfoType.PLAYLIST -> Icons.AutoMirrored.Rounded.List

                                // Artist or Channel
                                InfoItem.InfoType.CHANNEL -> Icons.Rounded.Person

                                // Comment (unused in here but kept for exhaustive branching)
                                InfoItem.InfoType.COMMENT -> Icons.AutoMirrored.Rounded.Comment
                            }
                        ),
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(
                            LocalContext.current
                        ).crossfade(true)
                            .data(mediaItems[index].thumbnailUri)
                            .build(),
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
                            text = mediaItems[index].artist
                                ?: "Unknown Artist",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = menuVisible,
                onDismissRequest = { menuVisible = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Enqueue") },
                    onClick = { onEnqueue(mediaItems[index]); menuVisible = false }
                )
                DropdownMenuItem(
                    text = { Text("Enqueue Next") },
                    onClick = { onEnqueueNext(mediaItems[index]); menuVisible = false }
                )
                DropdownMenuItem(
                    text = { Text("Enqueue Radio") },
                    onClick = { onEnqueueRadio(mediaItems[index]); menuVisible = false }
                )
            }

            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaListPreview() {
    MediaList(
        mediaItems = listOf(
            MediaListItem(
                title = "Song",
                artist = "Artist1",
                infoType = InfoItem.InfoType.STREAM,
                thumbnailUri = null,
                mediaUri = null
            ),
            MediaListItem(
                title = "Playlist",
                artist = "Artist2",
                infoType = InfoItem.InfoType.PLAYLIST,
                thumbnailUri = null,
                mediaUri = null
            ),
            MediaListItem(
                title = "Channel",
                artist = "Artist3",
                infoType = InfoItem.InfoType.CHANNEL,
                thumbnailUri = null,
                mediaUri = null
            )
        ),
        onItemClicked = { _, _ -> /* no-op */ },
        onEnqueue = { /* no-op */ },
        onEnqueueNext = { /* no-op */ },
        onEnqueueRadio = { /* no-op */ }
    )
}
