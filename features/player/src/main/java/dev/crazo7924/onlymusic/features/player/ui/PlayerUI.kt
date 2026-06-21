/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.player.ui

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.core.R as commonR
import dev.crazo7924.onlymusic.core.ui.components.forwardingPainter
import dev.crazo7924.onlymusic.features.player.PlaybackState
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.player.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerUI(
    playerUiState: PlayerUiState,
    onSeekTo: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onQueueIconClicked: () -> Unit,
    onCollapse: () -> Unit,
    onRadioIconClicked: () -> Unit,
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
                        contentDescription = stringResource(R.string.collapse_playerui_icon_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = playerUiState.media?.mediaMetadata?.artist?.toString()
                        ?: stringResource(commonR.string.song_unknown_artist),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = playerUiState.media?.mediaMetadata?.title?.toString() ?: stringResource(commonR.string.song_unknown_title),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2
                )
                val icon = forwardingPainter(
                    rememberVectorPainter(
                        Icons.Rounded.Album
                    ),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
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
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val duration = (playerUiState.media?.mediaMetadata?.durationMs ?: 0L).toFloat().coerceAtLeast(1f)
                val sliderState = remember(duration) {
                    SliderState(
                        value = playerUiState.position.toFloat(),
                        valueRange = 0F..duration
                    ).apply {
                        onValueChangeFinished = {
                            onSeekTo(value)
                        }
                    }
                }

                LaunchedEffect(playerUiState.position, sliderState) {
                    if (!sliderState.isDragging) {
                        sliderState.value = playerUiState.position.toFloat()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(
                        sliderState.value.toLong().toTimeString(), modifier = Modifier.align(
                            Alignment.CenterStart
                        )
                    )
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.size(36.dp), onClick = {
                                onPlayPrevious()
                            }) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = stringResource(R.string.go_previous_icon_description),
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
                                contentDescription = stringResource(R.string.toggle_playback_icon_description),
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
                                contentDescription = stringResource(R.string.go_next_icon_description),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                    }
                    Text(
                        (playerUiState.media?.mediaMetadata?.durationMs ?: 0L).toTimeString(),
                        modifier = Modifier.align(
                            Alignment.CenterEnd
                        )
                    )
                }

                Slider(
                    modifier = Modifier.padding(16.dp),
                    state = sliderState
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = onRadioIconClicked
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Radio,
                            contentDescription = stringResource(commonR.string.enqueue_radio),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = onQueueIconClicked,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = stringResource(R.string.queue_icon_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
        onCollapse = {},
        onRadioIconClicked = {}
    )
}
