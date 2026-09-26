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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.crazo7924.onlymusic.MediaControllerManager
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.schabi.newpipe.extractor.InfoItem
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PlayerServiceTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var controllerManager: MediaControllerManager
    private val mockPlayerViewModel: PlayerViewModel = mockk(relaxed = true)

    @Inject
    lateinit var musicRepository: MusicRepository

    @Before
    fun setup() {
        hiltRule.inject()
        clearMocks(musicRepository, answers = true, recordedCalls = true, childMocks = true)
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
        throw IllegalStateException("MediaController failed to connect within 5 seconds")
    }

    private fun createMediaListItem(
        id: String = "test_id",
        title: String = "Test Title",
        artist: String = "Test Artist",
        mediaUri: String = "https://example.com/test.mp3",
    ) = MediaListItem(
        id = id,
        title = title,
        artist = artist,
        infoType = InfoItem.InfoType.STREAM,
        thumbnailUri = "https://example.com/thumb.jpg",
        mediaUri = mediaUri,
        duration = 180000L,
    )

    private fun MediaController.awaitTimelineChange(
        expectedCount: Int,
        timeoutSeconds: Long = 5,
    ): Boolean {
        if (this.mediaItemCount == expectedCount) return true
        val latch = CountDownLatch(1)
        val listener = object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (mediaItemCount == expectedCount) {
                    latch.countDown()
                }
            }
        }
        addListener(listener)
        val result = if (mediaItemCount == expectedCount) {
            true
        } else {
            latch.await(timeoutSeconds, TimeUnit.SECONDS)
        }
        removeListener(listener)
        return result
    }

    private fun MediaController.awaitMediaItemTransition(
        expectedMediaId: String,
        timeoutSeconds: Long = 5,
    ): Boolean {
        if (currentMediaItem?.mediaId == expectedMediaId) return true
        val latch = CountDownLatch(1)
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem?.mediaId == expectedMediaId) {
                    latch.countDown()
                }
            }
        }
        addListener(listener)
        val result = if (currentMediaItem?.mediaId == expectedMediaId) {
            true
        } else {
            latch.await(timeoutSeconds, TimeUnit.SECONDS)
        }
        removeListener(listener)
        return result
    }

    @Test
    fun testLoadStreamUri_Success() = runBlocking(Dispatchers.Main.immediate) {
        val testUri = "https://example.com/stream1.mp3"
        val item = createMediaListItem(id = testUri, mediaUri = testUri)
        coEvery { musicRepository.loadMediaUri(testUri) } returns Result.success(item)

        val controller = awaitController()
        val args = Bundle().apply {
            putString(PlayerService.KEY_URI, testUri)
            putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
        }

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, args)

        val transitioned = controller.awaitMediaItemTransition(testUri)
        assertTrue("Player should have transitioned to test URI", transitioned)
        assertEquals(testUri, controller.currentMediaItem?.mediaId)
    }

    @Test
    fun testLoadStreamUri_Failure_DoesNotUpdatePlayer() = runBlocking(Dispatchers.Main.immediate) {
        val testUri = "https://example.com/error.mp3"
        coEvery { musicRepository.loadMediaUri(testUri) } returns Result.failure(RuntimeException("Network error"))

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
    fun testLoadStreamUri_MissingUriArg_HandledGracefully() = runBlocking(Dispatchers.Main.immediate) {
        val controller = awaitController()
        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, Bundle.EMPTY)

        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
    }

    @Test
    fun testLoadPlaylistUri_Success() = runBlocking(Dispatchers.Main.immediate) {
        val playlistUri = "https://example.com/playlist1"
        val item1 = createMediaListItem(id = "item1", mediaUri = "https://example.com/1.mp3")
        val item2 = createMediaListItem(id = "item2", mediaUri = "https://example.com/2.mp3")

        coEvery { musicRepository.loadPlaylistUri(playlistUri) } returns flowOf(
            Result.success(item1),
            Result.success(item2),
        )

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
    fun testLoadPlaylistUri_PartialFailure_OnlyLoadsSuccessfulItems() = runBlocking(Dispatchers.Main.immediate) {
        val playlistUri = "https://example.com/playlist_partial"
        val item1 = createMediaListItem(id = "p1", mediaUri = "https://example.com/p1.mp3")
        val item2 = createMediaListItem(id = "p2", mediaUri = "https://example.com/p2.mp3")

        coEvery { musicRepository.loadPlaylistUri(playlistUri) } returns flowOf(
            Result.success(item1),
            Result.failure(RuntimeException("Item failed")),
            Result.success(item2),
        )

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
    fun testLoadPlaylistUri_MissingPlaylistUriArg_HandledGracefully() = runBlocking(Dispatchers.Main.immediate) {
        val controller = awaitController()
        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_LOAD_PLAYLIST_URI, Bundle.EMPTY)

        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
    }

    @Test
    fun testEnqueueUri_Success() = runBlocking(Dispatchers.Main.immediate) {
        val uri1 = "https://example.com/e1.mp3"
        val uri2 = "https://example.com/e2.mp3"

        val item1 = createMediaListItem(id = uri1, mediaUri = uri1)
        val item2 = createMediaListItem(id = uri2, mediaUri = uri2)

        coEvery { musicRepository.loadMediaUri(uri1) } returns Result.success(item1)
        coEvery { musicRepository.loadMediaUri(uri2) } returns Result.success(item2)

        val controller = awaitController()

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, Bundle().apply { putString(PlayerService.KEY_URI, uri1) })
        controller.awaitTimelineChange(1)

        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_URI, Bundle().apply { putString(PlayerService.KEY_URI, uri2) })

        val updated = controller.awaitTimelineChange(2)
        assertTrue("Queue size should be 2 after enqueue", updated)
        assertEquals(2, controller.mediaItemCount)
    }

    @Test
    fun testEnqueueUri_Failure_DoesNotIncrementQueue() = runBlocking(Dispatchers.Main.immediate) {
        val uriError = "https://example.com/error_enqueue.mp3"
        coEvery { musicRepository.loadMediaUri(uriError) } returns Result.failure(RuntimeException("Enqueue failed"))

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_URI, Bundle().apply { putString(PlayerService.KEY_URI, uriError) })

        delay(500.milliseconds)
        assertEquals("Queue size should not increment on enqueue failure", initialCount, controller.mediaItemCount)
    }

    @Test
    fun testEnqueueNext_InsertsAfterCurrentItem() = runBlocking(Dispatchers.Main.immediate) {
        val uri1 = "https://example.com/item1.mp3"
        val uri2 = "https://example.com/item2.mp3"
        val uriNext = "https://example.com/itemNext.mp3"

        val item1 = createMediaListItem(id = uri1, mediaUri = uri1)
        val item2 = createMediaListItem(id = uri2, mediaUri = uri2)
        val itemNext = createMediaListItem(id = uriNext, mediaUri = uriNext)

        coEvery { musicRepository.loadMediaUri(uri1) } returns Result.success(item1)
        coEvery { musicRepository.loadMediaUri(uri2) } returns Result.success(item2)
        coEvery { musicRepository.loadMediaUri(uriNext) } returns Result.success(itemNext)

        val controller = awaitController()

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, Bundle().apply { putString(PlayerService.KEY_URI, uri1) })
        controller.awaitTimelineChange(1)
        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_URI, Bundle().apply { putString(PlayerService.KEY_URI, uri2) })
        controller.awaitTimelineChange(2)

        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_NEXT_URI, Bundle().apply { putString(PlayerService.KEY_URI, uriNext) })

        val updated = controller.awaitTimelineChange(3)
        assertTrue("Queue size should be 3 after enqueue next", updated)
        assertEquals(3, controller.mediaItemCount)
        assertEquals(uriNext, controller.getMediaItemAt(1).mediaId)
    }

    @Test
    fun testEnqueuePlaylistUri_Success() = runBlocking(Dispatchers.Main.immediate) {
        val playlistUri = "https://example.com/enqueue_playlist"
        val item1 = createMediaListItem(id = "ep1", mediaUri = "https://example.com/ep1.mp3")
        val item2 = createMediaListItem(id = "ep2", mediaUri = "https://example.com/ep2.mp3")

        coEvery { musicRepository.loadPlaylistUri(playlistUri) } returns flowOf(
            Result.success(item1),
            Result.success(item2),
        )

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_PLAYLIST_URI, Bundle().apply { putString(PlayerService.KEY_PLAYLIST_URI, playlistUri) })

        val updated = controller.awaitTimelineChange(initialCount + 2)
        assertTrue("Queue size should increment by playlist items count", updated)
    }

    @Test
    fun testSeekToPercentage_ValidPercentage() = runBlocking(Dispatchers.Main.immediate) {
        val testUri = "https://example.com/seek_test.mp3"
        val item = createMediaListItem(id = testUri, mediaUri = testUri)
        coEvery { musicRepository.loadMediaUri(testUri) } returns Result.success(item)

        val controller = awaitController()
        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, Bundle().apply { putString(PlayerService.KEY_URI, testUri) })
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
    fun testSeekToPercentage_InvalidPercentage_Ignored() = runBlocking(Dispatchers.Main.immediate) {
        val controller = awaitController()
        val seekArgsInvalid = Bundle().apply {
            putFloat(PlayerService.KEY_PERCENTAGE, 1.5f)
        }

        val resultFuture = controller.sendCustomCommand(PlayerService.COMMAND_SEEK_TO_PERCENTAGE, seekArgsInvalid)
        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
        assertEquals(SessionResult.RESULT_SUCCESS, result.resultCode)
    }

    @Test
    fun testEnqueueRadio_Success() = runBlocking(Dispatchers.Main.immediate) {
        val radioUri = "https://example.com/radio1"
        val item1 = createMediaListItem(id = "r1", mediaUri = "https://example.com/r1.mp3")
        val item2 = createMediaListItem(id = "r2", mediaUri = "https://example.com/r2.mp3")

        coEvery { musicRepository.loadAutoPlaylistUri(radioUri) } returns flowOf(
            Result.success(item1),
            Result.success(item2),
        )

        val controller = awaitController()
        controller.sendCustomCommand(PlayerService.COMMAND_ENQUEUE_RADIO, Bundle().apply { putString(PlayerService.KEY_URI, radioUri) })

        val updated = controller.awaitTimelineChange(2)
        assertTrue("Queue size should increase by radio items count", updated)
    }

    @Test
    fun testLoadMoreQueue_Success() = runBlocking(Dispatchers.Main.immediate) {
        val item = createMediaListItem(id = "lm1", mediaUri = "https://example.com/lm1.mp3")
        coEvery { musicRepository.loadMorePlaylistItems() } returns flowOf(Result.success(item))

        val controller = awaitController()
        val initialCount = controller.mediaItemCount

        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_MORE_QUEUE, Bundle.EMPTY)

        val updated = controller.awaitTimelineChange(initialCount + 1)
        assertTrue("Queue size should increase after load more", updated)
    }

    @Test
    fun testOnStartCommand_InitialStreamUri() = runBlocking(Dispatchers.Main.immediate) {
        val initialUri = "https://example.com/initial_stream.mp3"
        val item = createMediaListItem(id = "1", mediaUri = initialUri)
        coEvery { musicRepository.loadMediaUri(initialUri) } returns Result.success(item)

        val intent = Intent(context, PlayerService::class.java).apply {
            putExtra(PlayerService.EXTRA_INITIAL_STREAM_URI, initialUri)
            putExtra(PlayerService.EXTRA_INITIAL_AUTO_PLAY, true)
        }

        context.startService(intent)

        val controller = awaitController()
        val transitioned = controller.awaitMediaItemTransition("1")
        assertTrue("Service should load initial URI from intent", transitioned)
    }

    @Test
    fun testOnStartCommand_InitialPlaylistUri() = runBlocking(Dispatchers.Main.immediate) {
        val initialPlaylistUri = "https://example.com/initial_playlist"
        val item1 = createMediaListItem(id = "ip1", mediaUri = "https://example.com/ip1.mp3")

        coEvery { musicRepository.loadPlaylistUri(initialPlaylistUri) } returns flowOf(Result.success(item1))

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
    fun testOnStartCommand_NullOrEmptyIntent_HandledGracefully() = runBlocking(Dispatchers.Main.immediate) {
        val intent = Intent(context, PlayerService::class.java)
        context.startService(intent)

        val controller = awaitController()
        assertNotNull(controller)
    }

    @Test
    fun testUnknownCustomCommand_ReturnsPermissionDenied() = runBlocking(Dispatchers.Main.immediate) {
        val controller = awaitController()
        val unknownCommand = SessionCommand("dev.crazo7924.onlymusic.player.UNKNOWN_COMMAND", Bundle.EMPTY)

        val resultFuture = controller.sendCustomCommand(unknownCommand, Bundle.EMPTY)
        val result = resultFuture.get(5, TimeUnit.SECONDS)
        assertNotNull(result)
        assertEquals(SessionError.ERROR_PERMISSION_DENIED, result.resultCode)
    }

    @Test
    fun testSaveToRecents_TriggeredAfterPlaying() = runBlocking(Dispatchers.Main.immediate) {
        val testUri = "https://example.com/recents_test.mp3"
        val item = createMediaListItem(id = testUri, mediaUri = testUri)
        coEvery { musicRepository.loadMediaUri(testUri) } returns Result.success(item)
        coEvery { musicRepository.saveToRecents(any()) } returns Unit

        val controller = awaitController()
        val loadArgs = Bundle().apply {
            putString(PlayerService.KEY_URI, testUri)
            putBoolean(PlayerService.KEY_PLAY_WHEN_READY, true)
        }
        controller.sendCustomCommand(PlayerService.COMMAND_LOAD_STREAM_URI, loadArgs)

        controller.awaitMediaItemTransition(testUri)

        // PlayerService delays 3000ms before saving to recents
        delay(3500.milliseconds)

        coVerify { musicRepository.saveToRecents(any()) }
    }
}
