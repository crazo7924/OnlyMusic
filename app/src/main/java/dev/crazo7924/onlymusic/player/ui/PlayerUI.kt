package dev.crazo7924.onlymusic.player.ui

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Downloading
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.forwardingPainter
import dev.crazo7924.onlymusic.player.PlaybackState
import dev.crazo7924.onlymusic.player.PlayerUiState

@Composable
fun PlayerUI(
    playerUiState: PlayerUiState,
    onSeekTo: (Float) -> Unit,
    onPerformSeek: () -> Unit,
    onPlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onQueueIconClicked: () -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                Text(
                    text = playerUiState.media?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
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
                    ),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
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

                IconButton(onClick = onQueueIconClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.primary
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
                        IconButton(onClick = {
                            onPlayPrevious()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            onClick = {
                                onPlayPause()
                            }
                        ) {
                            val icon =
                                when (playerUiState.playbackState) {
                                    PlaybackState.INITIAL -> Icons.Rounded.PlayArrow
                                    PlaybackState.LOADING -> Icons.Rounded.Downloading
                                    PlaybackState.PLAYING -> Icons.Rounded.Pause
                                    PlaybackState.PAUSED -> Icons.Rounded.PlayArrow
                                    PlaybackState.STOPPED -> Icons.Rounded.RestartAlt
                                }
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = icon,
                                contentDescription = "Toggle playback",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            onPlayNext()
                        }) {
                            Icon(
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
                    onValueChange = { onSeekTo(it) },
                    onValueChangeFinished = onPerformSeek
                )
            }
        }
    }
}

private fun Long.toTimeString(): String {
    var hours = 0L
    var minutes = 0L
    var seconds = this / 1000
    if (seconds >= 60) {
        minutes = seconds / 60
        seconds %= 60
    }

    if (minutes >= 60) {
        hours = minutes / 60
        minutes %= 60
    }

    val hh = if (hours >= 10) "$hours" else "0$hours"
    val mm = if (minutes >= 10) "$minutes" else "0$minutes"
    val ss = if (seconds >= 10) "$seconds" else "0$seconds"

    if (hh == "00") {
        return "$mm:$ss"
    }
    return "$hh:$mm:$ss"
}

val CenteredSquareShape: Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        Log.d("CenteredSquareShape", "size: $size")
        val minSide = minOf(size.width, size.height)
        val maxSide = maxOf(size.width, size.height)
        val rect = RoundRect(
            (maxSide - minSide) / 2,
            0F,
            (maxSide - minSide) / 2 + minSide,
            minSide,
            cornerRadius = CornerRadius(16)
        )
        Log.d("CenteredSquareShape", "rect: $rect")
        return Outline.Rounded(rect)
    }

    override fun toString(): String = "CenteredSquareShape"
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
        onPerformSeek = {},
        onQueueIconClicked = {}
    )
}

