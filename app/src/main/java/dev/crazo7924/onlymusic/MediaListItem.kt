package dev.crazo7924.onlymusic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem

@Preview
@Composable
fun MediaListItem(mediaItem: MediaItem? = null) {
    Card(shape = RectangleShape) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .height(56.dp)
                .fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier
                    .size(56.dp),
                imageVector = Icons.Outlined.Album,
                contentDescription = "Album artwork",
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Title",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    mediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}