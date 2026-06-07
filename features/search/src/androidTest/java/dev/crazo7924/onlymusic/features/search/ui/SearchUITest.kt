package dev.crazo7924.onlymusic.features.search.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import dev.crazo7924.onlymusic.features.search.SearchState
import dev.crazo7924.onlymusic.features.search.SearchUiState
import org.junit.Rule
import org.junit.Test

class SearchUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialState_showsPlaceholderText() {
        composeTestRule.setContent {
            SearchUI(
                searchUiState = SearchUiState(searchState = SearchState.INITIAL),
                onItemClicked = {},
                onEnqueue = {},
                onEnqueueRadio = {},
                onEnqueueNext = {},
                onSearch = {},
                onSearchQueryUpdated = {}
            )
        }

        composeTestRule.onNodeWithText("Search for music you love").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorText() {
        composeTestRule.setContent {
            SearchUI(
                searchUiState = SearchUiState(searchState = SearchState.ERROR),
                onItemClicked = {},
                onEnqueue = {},
                onEnqueueRadio = {},
                onEnqueueNext = {},
                onSearch = {},
                onSearchQueryUpdated = {}
            )
        }

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }
}
