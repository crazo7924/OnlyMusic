package dev.crazo7924.onlymusic

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import dev.crazo7924.onlymusic.features.search.SearchUiState
import dev.crazo7924.onlymusic.features.search.SearchViewModel
import dev.crazo7924.onlymusic.ui.main.MainScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun renderMainScreen_initialStates() {
        val mediaControllerManager: MediaControllerManager = mockk(relaxed = true)
        val playerViewModel: PlayerViewModel = mockk(relaxed = true)
        val searchViewModel: SearchViewModel = mockk(relaxed = true)

        every { playerViewModel.uiState } returns MutableStateFlow(PlayerUiState())
        every { searchViewModel.uiState } returns MutableStateFlow(SearchUiState())

        composeTestRule.setContent {
            MainScreen(
                playerViewModel = playerViewModel,
                searchViewModel = searchViewModel,
                mediaControllerManager = mediaControllerManager
            )
        }

        // Verify Search UI is displayed by checking for its placeholder text
        composeTestRule.onNodeWithText("Search for music you love!").assertIsDisplayed()
    }
}
