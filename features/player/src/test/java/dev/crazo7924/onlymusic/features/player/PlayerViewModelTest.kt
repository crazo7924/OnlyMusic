package dev.crazo7924.onlymusic.features.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.Timeline
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerViewModelTest {

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        viewModel = PlayerViewModel()
    }

    @Test
    fun `updatePosition updates uiState position`() {
        viewModel.updatePosition(1500L)
        assertEquals(1500L, viewModel.uiState.value.position)
    }

    @Test
    fun `updateDuration updates uiState duration`() {
        viewModel.updateDuration(5000L)
        assertEquals(5000L, viewModel.uiState.value.duration)
    }

    @Test
    fun `setError updates uiState correctly`() {
        val errorMsg = "Network Timeout"
        viewModel.setError(errorMsg)
        assertEquals(PlaybackState.ERROR, viewModel.uiState.value.playbackState)
        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updateStateFromPlayer maps playing state correctly`() {
        val player = mockk<Player>()
        val mockTimeline = mockk<Timeline>()

        every { player.playbackState } returns Player.STATE_READY
        every { player.playWhenReady } returns true
        every { player.currentMediaItem } returns null
        every { player.currentMediaItemIndex } returns 0
        every { mockTimeline.windowCount } returns 0
        every { player.currentTimeline } returns mockTimeline
        every { player.currentPosition } returns 1000L
        every { player.duration } returns 5000L
        every { player.playerError } returns null

        viewModel.updateStateFromPlayer(player)

        assertEquals(PlaybackState.PLAYING, viewModel.uiState.value.playbackState)
        assertEquals(1000L, viewModel.uiState.value.position)
        assertEquals(5000L, viewModel.uiState.value.duration)
    }

    @Test
    fun `updateStateFromPlayer maps paused state correctly`() {
        val player = mockk<Player>()
        val mockTimeline = mockk<Timeline>()

        every { player.playbackState } returns Player.STATE_READY
        every { player.playWhenReady } returns false
        every { player.currentMediaItem } returns null
        every { player.currentMediaItemIndex } returns 0
        every { mockTimeline.windowCount } returns 0
        every { player.currentTimeline } returns mockTimeline
        every { player.currentPosition } returns 1000L
        every { player.duration } returns 5000L
        every { player.playerError } returns null

        viewModel.updateStateFromPlayer(player)

        assertEquals(PlaybackState.PAUSED, viewModel.uiState.value.playbackState)
    }
}
