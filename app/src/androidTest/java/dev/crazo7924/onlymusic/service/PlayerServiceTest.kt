/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.crazo7924.onlymusic.MediaControllerManager
import dev.crazo7924.onlymusic.TestRepositoryModule
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.schabi.newpipe.extractor.InfoItem
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PlayerServiceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var controllerManager: MediaControllerManager
    private val mockPlayerViewModel: PlayerViewModel = mockk(relaxed = true)
    private val fakeMusicRepository = TestRepositoryModule.fakeMusicRepository

    private fun runMainTest(block: suspend CoroutineScope.() -> Unit) =
        runBlocking(Dispatchers.Main.immediate, block)

    @Before
    fun setup() {
        hiltRule.inject()
        fakeMusicRepository.resetDefaults()
        context = ApplicationProvider.getApplicationContext()

        // Mock LifecycleScope for the manager
        val mockScope = mockk<LifecycleCoroutineScope>(relaxed = true)

        controllerManager = MediaControllerManager(
            context = context,
            playerViewModel = mockPlayerViewModel,
            lifecycleScope = mockScope,
        )
        controllerManager.initialize()
    }

    @After
    fun tearDown() {
        val mainHandler = android.os.Handler(Looper.getMainLooper())
        mainHandler.post {
            controllerManager.release()
        }
    }

    private suspend fun awaitController(): MediaController {
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            val controller = controllerManager.getController()
            if ((controller != null) && controller.isConnected) {
                return controller
            }
            delay(50.milliseconds)
        }
        error("MediaController failed to connect within 5 seconds")
    }

    private fun createMediaListItem(
        title: String = "Test Title",
        artist: String = "Test Artist",
        mediaUri: String = "https://example.com/test.mp3",
    ) = MediaListItem(
        id = UUID.randomUUID().toString(),
        title = title,
        artist = artist,
        infoType = InfoItem.InfoType.STREAM,
        thumbnailUri = "https://example.com/thumb.jpg",
        mediaUri = mediaUri,
        duration = 180000L,
    )

    private suspend fun MediaController.awaitTimelineChange(
        expectedCount: Int,
        timeoutSeconds: Long = 5,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutSeconds * 1000
        while (System.currentTimeMillis() < deadline) {
            if (this.mediaItemCount == expectedCount) return true
            delay(50.milliseconds)
        }
        return this.mediaItemCount == expectedCount
    }

    private suspend fun MediaController.awaitMediaItemTransition(
        expectedMediaUri: String,
        timeoutSeconds: Long = 5,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutSeconds * 1000
        while (System.currentTimeMillis() < deadline) {
            val currentUri = currentMediaItem?.localConfiguration?.uri?.toString()
                ?: currentMediaItem?.requestMetadata?.mediaUri?.toString()
                ?: currentMediaItem?.mediaId
            if (currentUri == expectedMediaUri) return true
            delay(50.milliseconds)
        }
        val finalUri = currentMediaItem?.localConfiguration?.uri?.toString()
            ?: currentMediaItem?.requestMetadata?.mediaUri?.toString()
            ?: currentMediaItem?.mediaId
        return finalUri == expectedMediaUri
    }

    @Test
    fun testLoadStreamUri_Success() = runMainTest {
        val testUri = "https://example.com/stream1.mp3"
        val item = createMediaListItem(mediaUri = testUri)
        fakeMusicRepository.loadMediaUriLambda = { uri ->
            if (uri == testUri) Result.success(item) else Result.failure(Exception("Not found"))
        }

        val controller = awaitController()
        val args = Bundle().apply {
            putString(PlayerService.KEY_URI, testUri)
            putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
        }

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, args)

        val transitioned = controller.awaitMediaItemTransition(testUri)
        assertTrue("Player should have transitioned to test URI", transitioned)
        assertEquals(item.id, controller.currentMediaItem?.mediaId)
    }

    @Test
    fun testLoadStreamUri_Failure_DoesNotUpdatePlayer() = runMainTest {
        val testUri = "https://example.com/error.mp3"
        fakeMusicRepository.loadMediaUriLambda = { Result.failure(RuntimeException("Network error")) }

        val controller = awaitController()
        val initialCount = controller.mediaItemCount
        val args = Bundle().apply {
            putString(PlayerService.KEY_URI, testUri)
        }

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, args)

        delay(500.milliseconds)
        assertEquals("Queue count should remain unchanged on failure", initialCount, controller.mediaItemCount)
    }

    @Test
    fun testLoadStreamUri_MissingUriArg_HandledGracefully() = runMainTest {
        val controller = awaitController()
        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, Bundle.EMPTY)

        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
    }

    @Test
    fun testLoadPlaylistUri_Success() = runMainTest {
        val playlistUri = "https://example.com/playlist1"
        val item1 = createMediaListItem(mediaUri = "https://example.com/1.mp3")
        val item2 = createMediaListItem(mediaUri = "https://example.com/2.mp3")

        fakeMusicRepository.loadPlaylistUriLambda = {
            flowOf(Result.success(item1), Result.success(item2))
        }

        val controller = awaitController()
        val args = Bundle().apply {
            putString(PlayerService.KEY_PLAYLIST_URI, playlistUri)
            putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
        }

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_PLAYLIST_URI, args)

        val updated = controller.awaitTimelineChange(2)
        assertTrue("Queue size should become 2 for loaded playlist", updated)
        assertEquals(2, controller.mediaItemCount)
    }

    @Test
    fun testLoadPlaylistUri_PartialFailure_OnlyLoadsSuccessfulItems() = runMainTest {
        val playlistUri = "https://example.com/playlist_partial"
        val item1 = createMediaListItem(mediaUri = "https://example.com/p1.mp3")
        val item2 = createMediaListItem(mediaUri = "https://example.com/p2.mp3")

        fakeMusicRepository.loadPlaylistUriLambda = {
            flowOf(
                Result.success(item1),
                Result.failure(RuntimeException("Item failed")),
                Result.success(item2),
            )
        }

        val controller = awaitController()
        val args = Bundle().apply {
            putString(PlayerService.KEY_PLAYLIST_URI, playlistUri)
        }

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_PLAYLIST_URI, args)

        val updated = controller.awaitTimelineChange(2)
        assertTrue("Queue size should be 2, ignoring failed items", updated)
        assertEquals(2, controller.mediaItemCount)
    }

    @Test
    fun testLoadPlaylistUri_MissingPlaylistUriArg_HandledGracefully() = runMainTest {
        val controller = awaitController()
        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_LOAD_PLAYLIST_URI, Bundle.EMPTY)

        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
    }

    @Test
    fun testEnqueueUri_Success() = runMainTest {
        val uri1 = "https://example.com/e1.mp3"
        val uri2 = "https://example.com/e2.mp3"

        val item1 = createMediaListItem(mediaUri = uri1)
        val item2 = createMediaListItem(mediaUri = uri2)

        fakeMusicRepository.loadMediaUriLambda = { uri ->
            when (uri) {
                uri1 -> Result.success(item1)
                uri2 -> Result.success(item2)
                else -> Result.failure(Exception("Unknown uri"))
            }
        }

        val controller = awaitController()

        controller.sendCustomCommand(
            PlayerService.COMMAND_LOAD_STREAM_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uri1) }
        )
        controller.awaitTimelineChange(1)

        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uri2) }
        )

        val updated = controller.awaitTimelineChange(2)
        assertTrue("Queue size should be 2 after enqueue", updated)
        assertEquals(2, controller.mediaItemCount)
    }

    @Test
    fun testEnqueueUri_Failure_DoesNotIncrementQueue() = runMainTest {
        val uriError = "https://example.com/error_enqueue.mp3"
        fakeMusicRepository.loadMediaUriLambda = { Result.failure(RuntimeException("Enqueue failed")) }

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uriError) }
        )

        delay(500.milliseconds)
        assertEquals("Queue size should not increment on enqueue failure", initialCount, controller.mediaItemCount)
    }

    @Test
    fun testEnqueueNext_InsertsAfterCurrentItem() = runMainTest {
        val uri1 = "https://example.com/item1.mp3"
        val uri2 = "https://example.com/item2.mp3"
        val uriNext = "https://example.com/itemNext.mp3"

        val item1 = createMediaListItem(mediaUri = uri1)
        val item2 = createMediaListItem(mediaUri = uri2)
        val itemNext = createMediaListItem(mediaUri = uriNext)

        fakeMusicRepository.loadMediaUriLambda = { uri ->
            when (uri) {
                uri1 -> Result.success(item1)
                uri2 -> Result.success(item2)
                uriNext -> Result.success(itemNext)
                else -> Result.failure(Exception("Unknown uri"))
            }
        }

        val controller = awaitController()

        controller.sendCustomCommand(
            PlayerService.COMMAND_LOAD_STREAM_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uri1) }
        )
        controller.awaitTimelineChange(1)
        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uri2) }
        )
        controller.awaitTimelineChange(2)

        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_NEXT_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, uriNext) }
        )

        val updated = controller.awaitTimelineChange(3)
        assertTrue("Queue size should be 3 after enqueue next", updated)
        assertEquals(3, controller.mediaItemCount)
        assertEquals(itemNext.id, controller.getMediaItemAt(1).mediaId)
    }

    @Test
    fun testEnqueuePlaylistUri_Success() = runMainTest {
        val playlistUri = "https://example.com/enqueue_playlist"
        val item1 = createMediaListItem(mediaUri = "https://example.com/ep1.mp3")
        val item2 = createMediaListItem(mediaUri = "https://example.com/ep2.mp3")

        fakeMusicRepository.loadPlaylistUriLambda = {
            flowOf(Result.success(item1), Result.success(item2))
        }

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_PLAYLIST_URI,
            Bundle().apply { putString(PlayerService.KEY_PLAYLIST_URI, playlistUri) }
        )

        val updated = controller.awaitTimelineChange(initialCount + 2)
        assertTrue("Queue size should increment by playlist items count", updated)
    }

    @Test
    fun testSeekToPercentage_ValidPercentage() = runMainTest {
        val testUri = "https://example.com/seek_test.mp3"
        val item = createMediaListItem(mediaUri = testUri)
        fakeMusicRepository.loadMediaUriLambda = { Result.success(item) }

        val controller = awaitController()
        controller.sendCustomCommand(
            PlayerService.COMMAND_LOAD_STREAM_URI,
            Bundle().apply { putString(PlayerService.KEY_URI, testUri) }
        )
        controller.awaitTimelineChange(1)

        val seekArgs = Bundle().apply {
            putFloat(PlayerService.KEY_PERCENTAGE, 0.5f)
        }

        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_SEEK_TO_PERCENTAGE, seekArgs)
        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
        assertEquals(SessionResult.RESULT_SUCCESS, result.resultCode)
    }

    @Test
    fun testSeekToPercentage_InvalidPercentage_Handled() = runMainTest {
        val controller = awaitController()
        val seekArgsInvalid = Bundle().apply {
            putFloat(PlayerService.KEY_PERCENTAGE, 1.5f)
        }

        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_SEEK_TO_PERCENTAGE, seekArgsInvalid)
        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
        assertEquals(SessionResult.RESULT_ERROR_BAD_VALUE, result.resultCode)
    }

    @Test
    fun testEnqueueRadio_Success() = runMainTest {
        val radioUri = "https://example.com/radio1"
        val item1 = createMediaListItem(mediaUri = "https://example.com/r1.mp3")
        val item2 = createMediaListItem(mediaUri = "https://example.com/r2.mp3")

        fakeMusicRepository.loadAutoPlaylistUriLambda = {
            flowOf(Result.success(item1), Result.success(item2))
        }

        val controller = awaitController()
        val initialCount = controller.mediaItemCount
        controller.sendCustomCommand(
            PlayerService.COMMAND_ENQUEUE_RADIO,
            Bundle().apply { putString(PlayerService.KEY_URI, radioUri) }
        )

        val updated = controller.awaitTimelineChange(initialCount + 2)
        assertTrue("Queue size should increase by radio items count", updated)
    }

    @Test
    fun testLoadMoreQueue_Success() = runMainTest {
        val item = createMediaListItem(mediaUri = "https://example.com/lm1.mp3")
        fakeMusicRepository.loadMorePlaylistItemsLambda = { flowOf(Result.success(item)) }

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_MORE_QUEUE, Bundle.EMPTY)

        val updated = controller.awaitTimelineChange(initialCount + 1)
        assertTrue("Queue size should increase after load more", updated)
    }

    @Test
    fun testOnStartCommand_InitialStreamUri() = runMainTest {
        val initialUri = "https://example.com/initial_stream.mp3"
        val item = createMediaListItem(mediaUri = initialUri)
        fakeMusicRepository.loadMediaUriLambda = { uri ->
            if (uri == initialUri) Result.success(item) else Result.failure(Exception("Unknown uri"))
        }

        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra(PlayerService.EXTRA_INITIAL_STREAM_URI, initialUri)
            putExtra(PlayerService.EXTRA_INITIAL_AUTO_PLAY, true)
        }

        context.startService(intent)

        val controller = awaitController()
        val transitioned = controller.awaitMediaItemTransition(initialUri)
        assertTrue("Service should load initial URI from intent", transitioned)
    }

    @Test
    fun testOnStartCommand_InitialPlaylistUri() = runMainTest {
        val initialPlaylistUri = "https://example.com/initial_playlist"
        val item1 = createMediaListItem(mediaUri = "https://example.com/ip1.mp3")

        fakeMusicRepository.loadPlaylistUriLambda = { flowOf(Result.success(item1)) }

        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra(PlayerService.EXTRA_INITIAL_PLAYLIST_URI, initialPlaylistUri)
            putExtra(PlayerService.EXTRA_INITIAL_AUTO_PLAY, true)
        }

        context.startService(intent)

        val controller = awaitController()
        val updated = controller.awaitTimelineChange(1)
        assertTrue("Service should load initial playlist URI from intent", updated)
    }

    @Test
    fun testOnStartCommand_NullOrEmptyIntent_HandledGracefully() = runMainTest {
        val intent = Intent(context, PlayerService::class.java)
        context.startService(intent)

        val controller = awaitController()
        assertNotNull(controller)
    }

    @Test
    fun testUnknownCustomCommand_ReturnsPermissionDenied() = runMainTest {
        val controller = awaitController()
        val unknownCommand = SessionCommand("dev.crazo7924.onlymusic.player.UNKNOWN_COMMAND", Bundle.EMPTY)

        val resultFuture = controller.sendCustomCommand(unknownCommand, Bundle.EMPTY)
        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
        assertEquals(SessionError.ERROR_PERMISSION_DENIED, result.resultCode)
    }

    @Test
    fun testSaveToRecents_TriggeredAfterPlaying() = runMainTest {
        val testUri = "https://example.com/recents_test.mp3"
        val item = createMediaListItem(mediaUri = testUri)
        fakeMusicRepository.loadMediaUriLambda = { Result.success(item) }

        var savedItem: MediaListItem? = null
        fakeMusicRepository.saveToRecentsLambda = { savedItem = it }

        val controller = awaitController()
        val loadArgs = Bundle().apply {
            putString(PlayerService.KEY_URI, testUri)
            putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
        }
        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, loadArgs)

        controller.awaitMediaItemTransition(testUri)

        // PlayerService delays 3000ms before saving to recents
        delay(3500.milliseconds)

        assertNotNull(savedItem)
    }
}
