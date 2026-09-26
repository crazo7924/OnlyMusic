/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic

import android.os.Bundle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.media3.session.MediaController
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.features.player.ui.QueueUI
import dev.crazo7924.onlymusic.service.PlayerService
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.schabi.newpipe.extractor.InfoItem

class QueuePaginationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyLoadMoreTriggeredWhenScrollingToBottom() {
        // 1. Setup Mocks
        val controller = mockk<MediaController>(relaxed = true)
        var loadMoreCalled = false

        // Create a sufficiently large list to ensure we aren't already at the bottom
        // QueueUI triggers load more when lastVisibleItemIndex >= totalItemsCount - 5
        val items = (1..30).map { i ->
            MediaListItem(
                id = i.toString(),
                title = "Song $i",
                artist = "Artist $i",
                infoType = InfoItem.InfoType.STREAM,
                thumbnailUri = null,
                mediaUri = null
            )
        }

        // 2. Set Content
        composeTestRule.setContent {
            QueueUI(
                items = items,
                currentIndex = 0,
                onItemClicked = {},
                onLoadMore = {
                    loadMoreCalled = true
                    controller.sendCustomCommand(
                        PlayerService.COMMAND_LOAD_MORE_QUEUE,
                        Bundle.EMPTY
                    )
                }
            )
        }

        // 3. Verify initial state
        composeTestRule.onNodeWithText("Song 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Song 30").assertIsNotDisplayed()

        // 4. Robust Scrolling using testTag
        composeTestRule.onNodeWithTag("queue_list").performScrollToIndex(25)
        composeTestRule.waitForIdle()

        // 5. Final Verification
        composeTestRule.onNodeWithText("Song 30").assertIsDisplayed()

        assert(loadMoreCalled) { "onLoadMore callback should have been triggered" }

        verify {
            controller.sendCustomCommand(PlayerService.COMMAND_LOAD_MORE_QUEUE, Bundle.EMPTY)
        }
    }
}