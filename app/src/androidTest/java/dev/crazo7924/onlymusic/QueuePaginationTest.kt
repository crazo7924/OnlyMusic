package dev.crazo7924.onlymusic

import android.os.Bundle
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.media3.session.SessionCommand
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import dev.crazo7924.onlymusic.features.search.SearchUiState
import dev.crazo7924.onlymusic.features.search.SearchViewModel
import dev.crazo7924.onlymusic.service.PlayerService
import dev.crazo7924.onlymusic.ui.main.MainScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.schabi.newpipe.extractor.InfoItem

class QueuePaginationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyLoadMoreTriggeredOnScroll() {
        val mediaControllerManager: MediaControllerManager = mockk(relaxed = true)
        val playerViewModel: PlayerViewModel = mockk(relaxed = true)
        val searchViewModel: SearchViewModel = mockk(relaxed = true)
        val controller = mockk<androidx.media3.session.MediaController>(relaxed = true)

        every { mediaControllerManager.getController() } returns controller

        // Create a fake queue with 20 items
        val fakeQueue = (1..20).map {
            androidx.media3.common.MediaItem.Builder()
                .setMediaId(it.toString())
                .setMediaMetadata(androidx.media3.common.MediaMetadata.Builder()
                    .setTitle("Song $it")
                    .setArtist("Artist $it")
                    .build())
                .build()
        }

        every { playerViewModel.uiState } returns MutableStateFlow(
            PlayerUiState(
                queue = fakeQueue,
                currentMediaItemIndex = 0
            )
        )
        every { searchViewModel.uiState } returns MutableStateFlow(SearchUiState())

        composeTestRule.setContent {
            MainScreen(
                playerViewModel = playerViewModel,
                searchViewModel = searchViewModel,
                mediaControllerManager = mediaControllerManager
            )
        }

        // Initially we are on search screen, click to switch to queue
        composeTestRule.onNodeWithText("Queue").performClick()

        // Wait for items to be somewhat loaded
        composeTestRule.onNodeWithText("Song 1").assertIsDisplayed()

        // Scroll to the end of the list to trigger load more
        composeTestRule.onNodeWithText("Song 1").performTouchInput { swipeUp() }
        composeTestRule.onNodeWithText("Song 1").performTouchInput { swipeUp() }
        composeTestRule.onNodeWithText("Song 1").performTouchInput { swipeUp() }

        // Verify the custom command was sent
        verify(timeout = 3000) {
            controller.sendCustomCommand(PlayerService.COMMAND_LOAD_MORE_QUEUE, Bundle.EMPTY)
        }
    }
}
