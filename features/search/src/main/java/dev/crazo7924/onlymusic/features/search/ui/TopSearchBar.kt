/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.features.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.crazo7924.onlymusic.features.search.R.string

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    query: String,
    recentQueries: List<String> = emptyList(),
    querySuggestions: List<String> = emptyList(),
    onRecentQuerySelected: (String) -> Unit = {},
    onSuggestionSelected: (String) -> Unit = {},
    onDeleteRecentQuery: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    SearchBar(
        modifier = modifier,
        inputField = {
            TextField(
                keyboardActions = KeyboardActions(onSearch = {
                    expanded = false
                    onSearch()
                }),
                singleLine = true,
                suffix = {
                    if (query.isEmpty()) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(string.search_bar_indicator_icon_description)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(string.clear_search_query),
                            modifier = Modifier.clickable {
                                onQueryChange("")
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    if (!expanded) expanded = true
                },
                placeholder = {
                    Text(text = placeholder)
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        content = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (query.isBlank()) {
                    if (recentQueries.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(string.recent_searches),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(recentQueries) { recentQuery ->
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = {
                                    Text(text = recentQuery)
                                },
                                trailingContent = {
                                    IconButton(onClick = { onDeleteRecentQuery(recentQuery) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = stringResource(string.remove_search_query)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onRecentQuerySelected(recentQuery)
                                    }
                            )
                        }
                    }
                } else {
                    if (querySuggestions.isNotEmpty()) {
                        items(querySuggestions) { suggestion ->
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = {
                                    Text(text = suggestion)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        onSuggestionSelected(suggestion)
                                    }
                            )
                        }
                    }
                }
            }
        },
    )
}

@Preview(showSystemUi = true)
@Composable
fun TopSearchBarPreview() {
    var q: String by remember { mutableStateOf("") }
    TopSearchBar(
        onQueryChange = { q = it },
        onSearch = {/* no-op */ },
        placeholder = "Search",
        query = q,
        recentQueries = listOf("rock music", "jazz"),
        querySuggestions = listOf("rock music 2026", "rock music playlist")
    )
}
