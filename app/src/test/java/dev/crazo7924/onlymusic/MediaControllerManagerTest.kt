package dev.crazo7924.onlymusic

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

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
}
