package dev.crazo7924.onlymusic.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.crazo7924.onlymusic.player.PlaybackState
import dev.crazo7924.onlymusic.player.PlayerViewModel

@Composable
fun PlayerUI(modifier: Modifier = Modifier, viewModel: PlayerViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    Column(modifier = modifier) {
        Text(
            text = uiState.value.media?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = uiState.value.media?.mediaMetadata?.title?.toString() ?: "Unknown Title",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.displayMedium
        )
//        Image(uiState.value.media?.mediaMetadata?.artworkUri)
        Icon(
            tint = MaterialTheme.colorScheme.secondary,
            imageVector = Icons.Outlined.Album,
            contentDescription = "Current Album art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F)
                .padding(16.dp)
        )

        ElevatedCard(modifier = Modifier.padding(16.dp)) {
            Column {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Text((uiState.value.position).toTimeString())
                    Row(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(modifier = Modifier.size(48.dp), onClick = {
                            viewModel.onPlayPrevious()
                        }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(96.dp),
                            onClick = {
                                viewModel.onPlayPause()
                            }
                        ) {
                            val icon =
                                when (uiState.value.playbackState) {
                                    PlaybackState.INITIAL -> Icons.Filled.PlayArrow
                                    PlaybackState.LOADING -> Icons.Filled.Sync
                                    PlaybackState.PLAYING -> Icons.Filled.Pause
                                    PlaybackState.PAUSED -> Icons.Filled.PlayArrow
                                    PlaybackState.STOPPED -> Icons.Filled.RestartAlt
                                }
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = icon,
                                contentDescription = "Toggle playback",
                                tint = MaterialTheme.colorScheme.primary,

                                )
                        }

                        IconButton(onClick = {
                            viewModel.onPlayNext()
                        }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                    }
                    Text((uiState.value.duration).toTimeString())

                }

//                @OptIn(ExperimentalMaterial3Api::class)
                Slider(
                    modifier = Modifier.padding(16.dp),
                    value = if (uiState.value.duration == 0L) 0F else (uiState.value.position / uiState.value.duration).toFloat(),
                    onValueChange = {
                        viewModel.updatePlayerPosition()
                    },
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

