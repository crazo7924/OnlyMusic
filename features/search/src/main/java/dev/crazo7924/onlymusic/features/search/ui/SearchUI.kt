package dev.crazo7924.onlymusic.features.search.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.crazo7924.onlymusic.core.ui.components.MediaList
import dev.crazo7924.onlymusic.features.search.SearchState
import dev.crazo7924.onlymusic.features.search.SearchUiState
import dev.crazo7924.onlymusic.core.ui.components.shimmerLoading
import dev.crazo7924.onlymusic.core.ui.components.MediaListItem

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
                    text = "Search for music",
                    textAlign = TextAlign.Center
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


            SearchState.SUCCESS -> MediaList(
                modifier = Modifier.padding(innerPadding),
                mediaItems = searchUiState.suggestions,
                onItemClicked = { item: MediaListItem, _: Int ->
                    onItemClicked(item)
                },
                onEnqueue = {
                    onEnqueue(it)
                },
                onEnqueueNext = {
                    onEnqueueNext(it)
                },
                onEnqueueRadio = {
                    onEnqueueRadio(it)
                }
            )

            SearchState.ERROR -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Something went wrong",
                    textAlign = TextAlign.Center
                )
            }

            SearchState.LOADING -> MediaList(
                mediaItems = searchUiState.suggestions,
                onItemClicked = { _, _ -> /* no-op */ },
                onEnqueue = {/* no-op */ },
                onEnqueueNext = {/* no-op */ },
                onEnqueueRadio = {/* no-op */ }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SearchUIPreview() {
    SearchUI(
        searchUiState = SearchUiState(),
        onItemClicked = {},
        onEnqueue = {},
        onEnqueueNext = {},
        onSearch = {},
        onSearchQueryUpdated = {},
        onEnqueueRadio = {}
    )
}