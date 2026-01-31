package dev.crazo7924.onlymusic.features.player.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.features.player.PlaybackState
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.core.ui.components.forwardingPainter

@Composable
fun PlayerUI(
    playerUiState: PlayerUiState,
    onSeekTo: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onQueueIconClicked: () -> Unit,
    onCollapse: () -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse the player",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = playerUiState.media?.mediaMetadata?.artist?.toString()
                        ?: "Unknown Artist",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = playerUiState.media?.mediaMetadata?.title?.toString() ?: "Unknown Title",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2
                )
                val icon = forwardingPainter(
                    rememberVectorPainter(
                        Icons.Rounded.Album
                    ), colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                        .padding(16.dp)
                        .border(
                            shape = RoundedCornerShape(size = 8.dp),
                            color = Color.Transparent,
                            width = 0.dp
                        ), contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = playerUiState.media?.mediaMetadata?.artworkUri,
                        contentDescription = null,
                        placeholder = icon,
                        error = icon,
                        fallback = icon,
                        contentScale = ContentScale.Crop,
                        clipToBounds = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CenteredSquareShape)
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Text((playerUiState.position).toTimeString())
                    Row(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.size(36.dp), onClick = {
                                onPlayPrevious()
                            }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(64.dp), onClick = {
                                onPlayPause()
                            }) {
                            val icon = when (playerUiState.playbackState) {
                                PlaybackState.INITIAL -> Icons.Rounded.PlayArrow
                                PlaybackState.LOADING -> Icons.Rounded.Downloading
                                PlaybackState.PLAYING -> Icons.Rounded.Pause
                                PlaybackState.PAUSED -> Icons.Rounded.PlayArrow
                                PlaybackState.STOPPED -> Icons.Rounded.RestartAlt
                                PlaybackState.ERROR -> Icons.Rounded.Error
                            }
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = icon,
                                contentDescription = "Toggle playback",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(36.dp), onClick = {
                                onPlayNext()
                            }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                    }
                    Text((playerUiState.media?.mediaMetadata?.durationMs ?: 0L).toTimeString())

                }

                Slider(
                    modifier = Modifier.padding(16.dp),
                    valueRange = 0F..(playerUiState.media?.mediaMetadata?.durationMs?.toFloat()
                        ?: 1F),
                    value = playerUiState.position.toFloat(),
                    onValueChange = { onSeekTo(it) }
                )

                IconButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onQueueIconClicked,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayerPreview() {
    PlayerUI(
        playerUiState = PlayerUiState(),
        onSeekTo = {},
        onPlayPause = {},
        onPlayNext = {},
        onPlayPrevious = {},
        onQueueIconClicked = {},
        onCollapse = {})
}
