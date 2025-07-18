package dev.crazo7924.onlymusic.player.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.MediaList
import dev.crazo7924.onlymusic.MediaListItem
import dev.crazo7924.onlymusic.MediaListType
import dev.crazo7924.onlymusic.R
import dev.crazo7924.onlymusic.player.PlayerUiState
import dev.crazo7924.onlymusic.search.SearchState
import dev.crazo7924.onlymusic.search.SearchUiState
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.ui.TopSearchBar
import dev.crazo7924.onlymusic.shimmerLoading

@Composable
fun SearchUI(
    searchViewModel: SearchViewModel,
    searchUiState: SearchUiState,
    playerUiState: PlayerUiState,
    onItemClicked: (MediaListItem) -> Unit,
    onEnqueue: (MediaListItem) -> Unit,
    onEnqueueNext: (MediaListItem) -> Unit,
) {
    Scaffold(topBar = {
        TopSearchBar(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .statusBarsPadding(),
            query = searchUiState.query,
            onQueryChange = { searchViewModel.updateQueryFrom(it) },
            onSearch = { searchViewModel.search() },
            placeholder = stringResource(R.string.app_name),
            iconDescription = stringResource(R.string.search_bar_indicator_icon_description),
        )
    }, bottomBar = {
        Column(modifier = Modifier.Companion.padding(8.dp)) {
            ListItem(headlineContent = {
                if (playerUiState.media == null) Text("Nothing is playing")
                else Text(
                    text = playerUiState.media.mediaMetadata.title?.toString()
                        ?: "Unknown Title"
                )
            }, supportingContent = {
                if (playerUiState.media != null) Text(
                    text = playerUiState.media.mediaMetadata.artist?.toString()
                        ?: "Unknown Artist"
                ) else null
            }, leadingContent = {
                AsyncImage(
                    modifier = Modifier.Companion.size(48.dp),
                    model = playerUiState.media?.mediaMetadata?.artworkUri,
                    contentDescription = null
                )
            })
        }
    }) { innerPadding ->

        when (searchUiState.searchState) {
            SearchState.INITIAL -> Box(
                contentAlignment = Alignment.Companion.Center,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = stringResource(R.string.search_help_text),
                    textAlign = TextAlign.Companion.Center
                )
            }

            SearchState.SEARCHING -> Column(
                modifier = Modifier.Companion.padding(
                    innerPadding
                )
            ) {
                repeat(times = 8) {
                    ListItem(
                        headlineContent = {
                            Box(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerLoading()
                            )
                        })
                }
            }


            SearchState.SUCCESS -> MediaList(
                contentPadding = innerPadding,
                mediaListItems = searchUiState.suggestions,
                mediaListType = MediaListType.SUGGESTIONS,
                onItemClicked = {
                    onItemClicked(it)
                },
                onEnqueue = {
                    onEnqueue(it)
                },
                onEnqueueNext = {
                    onEnqueueNext(it)
                }
            )

            SearchState.ERROR -> Box(
                contentAlignment = Alignment.Companion.Center,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.something_went_wrong),
                    textAlign = TextAlign.Companion.Center
                )
            }
        }
    }
}