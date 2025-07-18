package dev.crazo7924.onlymusic

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.crazo7924.onlymusic.search.data.toTypeString
import org.schabi.newpipe.extractor.InfoItem

@Composable
fun MediaList(
    contentPadding: PaddingValues,
    mediaListItems: List<MediaListItem>,
    onItemClicked: (MediaListItem) -> Unit,
    onEnqueue: (MediaListItem) -> Unit,
    onEnqueueNext: (MediaListItem) -> Unit,
    mediaListType: MediaListType,
) {
    LazyColumn(contentPadding = contentPadding) {
        items(count = mediaListItems.size) { index ->
            var menuVisible by remember { mutableStateOf(false) }
            Box(contentAlignment = Alignment.TopStart) {
                ListItem(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onItemClicked(mediaListItems[index]) },
                            onLongClick = {
                                menuVisible = true
                            },
                        ),
                    leadingContent = {
                        val icon = forwardingPainter(
                            rememberVectorPainter(
                                when (mediaListItems[index].infoType) {
                                    InfoItem.InfoType.STREAM -> Icons.Outlined.MusicNote
                                    InfoItem.InfoType.PLAYLIST -> Icons.Outlined.Album
                                    InfoItem.InfoType.CHANNEL -> Icons.Outlined.Person
                                    InfoItem.InfoType.COMMENT -> Icons.AutoMirrored.Outlined.Comment
                                },
                            ),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(
                                LocalContext.current
                            ).crossfade(true)
                                .data(mediaListItems[index].thumbnailUri)
                                .build(),
                            contentDescription = null,
                            error = icon,
                            placeholder = icon
                        )
                    },
                    headlineContent = { Text(mediaListItems[index].title ?: "Unknown") },
                    supportingContent = {
                        when (mediaListType) {
                            MediaListType.QUEUE -> Text(
                                text = mediaListItems[index].artist ?: "Unknown Artist"
                            )

                            MediaListType.SUGGESTIONS -> Text(
                                text = mediaListItems[index].infoType.toTypeString()
                            )
                        }
                    },
                )
                DropdownMenu(
                    expanded = menuVisible,
                    onDismissRequest = { menuVisible = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Enqueue") },
                        onClick = { onEnqueue(mediaListItems[index]); menuVisible = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Enqueue Next") },
                        onClick = { onEnqueueNext(mediaListItems[index]); menuVisible = false }
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

data class MediaListItem(
    val title: String?,
    val artist: String? = null,
    val infoType: InfoItem.InfoType,
    val thumbnailUri: String?,
    val mediaUri: String? = null,
) {
    companion object {
        fun fromMediaItem(item: MediaItem): MediaListItem {
            return MediaListItem(
                title = item.mediaMetadata.title?.toString(),
                artist = item.mediaMetadata.artist?.toString(),
                infoType = InfoItem.InfoType.STREAM,
                thumbnailUri = item.mediaMetadata.artworkUri?.toString(),
                mediaUri = item.requestMetadata.mediaUri?.toString()
            )
        }
    }
}

fun Timeline?.toMediaListItem(): List<MediaListItem> {
    if (this == null) return listOf()
    val mediaListItem = mutableListOf<MediaListItem>()
    (0 until this.windowCount).forEach { i ->
        mediaListItem.add(
            MediaListItem.fromMediaItem(
                this.getWindow(
                    i,
                    Timeline.Window()
                ).mediaItem
            )
        )
    }
    return mediaListItem.toList()
}

enum class MediaListType {
    SUGGESTIONS,
    QUEUE
}

@Preview
@Composable
private fun MediaListPreview() {
    MediaList(
        PaddingValues(),
        mediaListItems = listOf(
            MediaListItem(
                title = "Title",
                artist = "Artist",
                infoType = InfoItem.InfoType.STREAM,
                thumbnailUri = null,
                mediaUri = null
            ),
            MediaListItem(
                title = "Title",
                artist = "Artist",
                infoType = InfoItem.InfoType.PLAYLIST,
                thumbnailUri = null,
                mediaUri = null
            ),
            MediaListItem(
                title = "Title",
                artist = "Artist",
                infoType = InfoItem.InfoType.CHANNEL,
                thumbnailUri = null,
                mediaUri = null
            )
        ),
        onItemClicked = {},
        onEnqueue = {},
        onEnqueueNext = {},
        mediaListType = MediaListType.QUEUE,
    )
}
