package dev.crazo7924.onlymusic.search.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.crazo7924.onlymusic.search.SearchState
import dev.crazo7924.onlymusic.search.data.SearchSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    minQueryLength: Int = 3,
    suggestions: List<SearchSuggestion>,
    searchState: SearchState,
) {
    DockedSearchBar(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .statusBarsPadding(), inputField = {
            TextField(
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                singleLine = true,
                suffix = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search bar indicator icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text("OnlyMusic")
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = true, keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
            )
        }, expanded = query.length >= minQueryLength, onExpandedChange = {/* no-op */ }) {
        when (searchState) {
            SearchState.INITIAL -> Text("Search for songs, artists, albums and playlists")
            SearchState.SEARCHING ->
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp))

            SearchState.SUCCESS -> LazyColumn {
                Log.d("TopSearchBar", "Success: ${suggestions.size}")
                items(count = suggestions.size) { index ->
                    ListItem(
                        leadingContent = {
                            AsyncImage(
                                model = suggestions[index].thumbnailUrl, contentDescription = null
                            )
                        },
                        headlineContent = { Text(suggestions[index].title) },
                        supportingContent = { Text(suggestions[index].type) })
                    HorizontalDivider()
                }
            }

            SearchState.ERROR -> Text("Something went wrong")
        }
    }
}


@Preview
@Composable
fun TopSearchBarPreview() {
    var q: String by remember { mutableStateOf("") }
    TopSearchBar(
        query = q,
        onQueryChange = { q = it },
        suggestions = listOf(),
        onSearch = {},
        searchState = SearchState.INITIAL
    )
}