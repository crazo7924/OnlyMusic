package dev.crazo7924.onlymusic

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.Futures
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MediaControllerManagerTest {

    private lateinit var mediaControllerManager: MediaControllerManager
    private val context: Context = RuntimeEnvironment.getApplication()
    private val playerViewModel: PlayerViewModel = mockk(relaxed = true)
    private val lifecycleScope: LifecycleCoroutineScope = mockk(relaxed = true)

    @Before
    fun setup() {
        mediaControllerManager = MediaControllerManager(context, playerViewModel, lifecycleScope)
    }

    @Test
    fun `initialize connects to MediaSession and adds listener`() {
        val mockBuilder = mockk<MediaController.Builder>()
        val mockController = mockk<MediaController>(relaxed = true)
        val future = Futures.immediateFuture(mockController)

        every { mockBuilder.buildAsync() } returns future

        // Use the internal factory variable for testing
        mediaControllerManager.mediaControllerBuilderFactory = { _, _ -> mockBuilder }

        mediaControllerManager.initialize()

        verify { playerViewModel.updateStateFromPlayer(mockController) }
        verify { mockController.addListener(any<Player.Listener>()) }
        assertNotNull(mediaControllerManager.getController())
    }

    @Test
    fun `release disconnects controller and removes listener`() {
        val mockBuilder = mockk<MediaController.Builder>()
        val mockController = mockk<MediaController>(relaxed = true)
        val future = Futures.immediateFuture(mockController)

        every { mockBuilder.buildAsync() } returns future
        mediaControllerManager.mediaControllerBuilderFactory = { _, _ -> mockBuilder }

        mediaControllerManager.initialize()
        mediaControllerManager.release()

        verify { mockController.release() }
        assert(mediaControllerManager.getController() == null)
    }

    @Test
    fun `listener updates view model on playback state change`() {
        val mockBuilder = mockk<MediaController.Builder>()
        val mockController = mockk<MediaController>(relaxed = true)
        val future = Futures.immediateFuture(mockController)
        val listenerSlot = slot<Player.Listener>()

        every { mockBuilder.buildAsync() } returns future
        mediaControllerManager.mediaControllerBuilderFactory = { _, _ -> mockBuilder }

        mediaControllerManager.initialize()
        verify { mockController.addListener(capture(listenerSlot)) }

        listenerSlot.captured.onPlaybackStateChanged(Player.STATE_READY)
        verify { playerViewModel.updateStateFromPlayer(mockController) }
    }

    @Test
    fun `listener updates view model on isPlaying change`() {
        val mockBuilder = mockk<MediaController.Builder>()
        val mockController = mockk<MediaController>(relaxed = true)
        val future = Futures.immediateFuture(mockController)
        val listenerSlot = slot<Player.Listener>()

        every { mockBuilder.buildAsync() } returns future
        mediaControllerManager.mediaControllerBuilderFactory = { _, _ -> mockBuilder }

        mediaControllerManager.initialize()
        verify { mockController.addListener(capture(listenerSlot)) }

        listenerSlot.captured.onIsPlayingChanged(true)
        verify { playerViewModel.updateStateFromPlayer(mockController) }
    }

    @Test
    fun `listener updates view model on media item transition`() {
        val mockBuilder = mockk<MediaController.Builder>()
        val mockController = mockk<MediaController>(relaxed = true)
        val future = Futures.immediateFuture(mockController)
        val listenerSlot = slot<Player.Listener>()

        every { mockBuilder.buildAsync() } returns future
        mediaControllerManager.mediaControllerBuilderFactory = { _, _ -> mockBuilder }

        mediaControllerManager.initialize()
        verify { mockController.addListener(capture(listenerSlot)) }

        listenerSlot.captured.onMediaItemTransition(null, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
        verify { playerViewModel.updateStateFromPlayer(mockController) }
    }
}
