package dev.crazo7924.onlymusic.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.forwardingPainter
import dev.crazo7924.onlymusic.player.PlaybackState
import dev.crazo7924.onlymusic.player.PlayerUiState

@Composable
fun PlayerUI(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onSeekTo: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = uiState.media?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = uiState.media?.mediaMetadata?.title?.toString() ?: "Unknown Title",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.displayMedium,
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
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uiState.media?.mediaMetadata?.artworkUri, contentDescription = null,
                placeholder = icon, error = icon,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CenteredSquareShape),
            )
        }

        OutlinedCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text((uiState.position).toTimeString())
                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(modifier = Modifier.requiredSize(48.dp), onClick = {
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
                        modifier = Modifier.requiredSize(96.dp),
                        onClick = {
                            onPlayPause()
                        }
                    ) {
                        val icon =
                            when (uiState.playbackState) {
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
                    }, modifier = Modifier.requiredSize(48.dp)) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                }
                Text((uiState.media?.mediaMetadata?.durationMs ?: 0L).toTimeString())

            }

            Slider(
                modifier = Modifier.padding(16.dp),
                valueRange = 0F..(uiState.media?.mediaMetadata?.durationMs?.toFloat() ?: 0F),
                value = uiState.position.toFloat(),
                onValueChange = {
                    onSeekTo(it)
                },
            )
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
        val minSide = minOf(size.width, size.height)
        val maxSide = maxOf(size.width, size.height)
        val rect = RoundRect(
            (maxSide - minSide) / 2,
            0F,
            (maxSide - minSide) / 2 + minSide,
            minSide,
            cornerRadius = CornerRadius(16)
        )
        return Outline.Rounded(rect)
    }

    override fun toString(): String = "CenteredSquareShape"
}

