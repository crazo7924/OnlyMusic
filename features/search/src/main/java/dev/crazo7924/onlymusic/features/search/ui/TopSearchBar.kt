package dev.crazo7924.onlymusic.features.search.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String,
    iconDescription: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    query: String,
) {
    DockedSearchBar(
        modifier = modifier,
        inputField = {
            TextField(
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                singleLine = true,
                suffix = {
                    Icon(
                        imageVector = Icons.Outlined.Search, contentDescription = iconDescription
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(text = placeholder)
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
        },
        expanded = false,
        onExpandedChange = { /* no-op */ },
        content = { /* no-op */ },
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
        iconDescription = "Search Icon",
        query = q
    )
}