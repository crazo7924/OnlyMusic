package dev.crazo7924.onlymusic

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import dev.crazo7924.onlymusic.ui.main.MainScreen
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.search.SearchUiState
import org.junit.Rule
import org.junit.Test
import io.mockk.mockk

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun renderMainScreen_initialStates() {
        val mediaControllerManager: MediaControllerManager = mockk(relaxed = true)

        composeTestRule.setContent {
            MainScreen(
                searchUiState = SearchUiState(),
                playerUiState = PlayerUiState(),
                searchViewModel = mockk(relaxed = true),
                mediaControllerManager = mediaControllerManager
            )
        }

        // Verify Search UI is displayed by checking for its placeholder text
        composeTestRule.onNodeWithText("Search for music you love").assertIsDisplayed()
    }
}
