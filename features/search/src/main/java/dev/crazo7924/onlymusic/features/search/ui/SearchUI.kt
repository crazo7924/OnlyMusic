/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.search.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.core.ui.components.iconForInfoType
import dev.crazo7924.onlymusic.features.search.SearchState
import dev.crazo7924.onlymusic.features.search.SearchUiState

@Composable
fun SearchUI(
    searchUiState: SearchUiState,
    onItemClicked: (MediaListItem) -> Unit,
    onEnqueue: (MediaListItem) -> Unit,
    onEnqueueRadio: (MediaListItem) -> Unit,
    onEnqueueNext: (MediaListItem) -> Unit,
    onSearch: () -> Unit,
    onSearchQueryUpdated: (String) -> Unit,
) {
    Scaffold(topBar = {
        TopSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(all = 8.dp),
            query = searchUiState.query,
            onQueryChange = onSearchQueryUpdated,
            onSearch = onSearch,
            placeholder = "OnlyMusic",
            iconDescription = "Search",
        )
    }) { innerPadding ->

        when (searchUiState.searchState) {
            SearchState.INITIAL -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Search for music you love!",
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                )
            }

            SearchState.SEARCHING -> Column(
                modifier = Modifier.padding(
                    innerPadding
                )
            ) {
                repeat(times = 8) {
                    ListItem(
                        headlineContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerLoading()
                            )
                        })
                }
            }


            SearchState.SUCCESS -> SearchList(
                modifier = Modifier.padding(innerPadding),
                mediaItems = searchUiState.suggestions,
                onItemClicked = { onItemClicked(it) },
                onEnqueue = { onEnqueue(it) },
                onEnqueueNext = { onEnqueueNext(it) },
                onEnqueueRadio = { onEnqueueRadio(it) })

            SearchState.ERROR -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Something went wrong :(", textAlign = TextAlign.Center
                )
            }

            SearchState.LOADING -> SearchList(
                mediaItems = searchUiState.suggestions,
                onItemClicked = { onItemClicked(it) },
                onEnqueue = { onEnqueue(it) },
                onEnqueueNext = { onEnqueueNext(it) },
                onEnqueueRadio = { onEnqueueRadio(it) })
        }
    }
}


@Composable
fun SearchList(
    modifier: Modifier = Modifier,
    mediaItems: List<MediaListItem>,
    onItemClicked: (MediaListItem) -> Unit,
    onEnqueue: (MediaListItem) -> Unit = {},
    onEnqueueNext: (MediaListItem) -> Unit = {},
    onEnqueueRadio: (MediaListItem) -> Unit = {},
) {
    LazyColumn(modifier = modifier) {
        items(count = mediaItems.size) { index ->
            var menuVisible by remember { mutableStateOf(false) }

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .combinedClickable(
                            onClick = { onItemClicked(mediaItems[index]) },
                            onLongClick = {
                                menuVisible = true
                            }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val intrinsicSize = with(LocalDensity.current) {
                        Size(48.dp.toPx(), 48.dp.toPx())
                    }

                    val icon = iconForInfoType(mediaItems[index].infoType, intrinsicSize)

                    AsyncImage(
                        modifier = Modifier.size(64.dp),
                        model = ImageRequest.Builder(
                            LocalContext.current
                        ).crossfade(true).data(mediaItems[index].thumbnailUri).build(),
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
                            text = mediaItems[index].artist ?: "Unknown Artist",
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
                    onClick = { onEnqueue(mediaItems[index]); menuVisible = false })
                DropdownMenuItem(
                    text = { Text("Enqueue Next") },
                    onClick = { onEnqueueNext(mediaItems[index]); menuVisible = false })
                DropdownMenuItem(
                    text = { Text("Enqueue Radio") },
                    onClick = { onEnqueueRadio(mediaItems[index]); menuVisible = false })
            }

            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    SearchUI(
        searchUiState = SearchUiState(),
        onItemClicked = {},
        onEnqueue = {},
        onEnqueueNext = {},
        onSearch = {},
        onSearchQueryUpdated = {},
        onEnqueueRadio = {},
    )
}