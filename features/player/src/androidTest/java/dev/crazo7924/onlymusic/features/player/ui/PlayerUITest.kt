package dev.crazo7924.onlymusic.features.player.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import dev.crazo7924.onlymusic.features.player.PlayerUiState
import dev.crazo7924.onlymusic.features.player.PlaybackState
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.junit.Rule
import org.junit.Test

class PlayerUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersTitleAndArtistCorrectly() {
        val mediaItem = MediaItem.Builder()
            .setMediaId("1")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Test Song")
                    .setArtist("Test Artist")
                    .build()
            )
            .build()

        composeTestRule.setContent {
            PlayerUI(
                playerUiState = PlayerUiState(media = mediaItem, playbackState = PlaybackState.PLAYING),
                onSeekTo = {},
                onPlayPause = {},
                onPlayNext = {},
                onPlayPrevious = {},
                onQueueIconClicked = {},
                onCollapse = {},
                onRadioIconClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Test Song").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Artist").assertIsDisplayed()
    }
}
