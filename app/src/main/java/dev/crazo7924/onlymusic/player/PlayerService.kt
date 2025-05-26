package dev.crazo7924.onlymusic.player

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.TaskStackBuilder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.crazo7924.onlymusic.MainActivity
import dev.crazo7924.onlymusic.repository.MusicRepository
import dev.crazo7924.onlymusic.repository.NewPipeMusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerService : MediaSessionService() {

    companion object {
        const val TAG = "PlayerService"
        const val STREAM_URI_ARG = "STREAM_URI"
        const val PLAYLIST_URI_ARG = "PLAYLIST_URI"
        const val ACTION_ARG = "ACTION"
        const val POSITION_ARG = "POSITION"
    }

    /** Our [ExoPlayer] instance - late initialization in [onCreate] */
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var musicRepository: MusicRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        Log.d(TAG, "Playback toggled")
    }

    fun stopPlayback() {
        exoPlayer.stop()
        Log.d(TAG, "Playback stopped.")
    }

    fun seekToNext() {
        exoPlayer.seekToNext()
        Log.d(TAG, "Next media will play")
    }


    fun seekToPrevious() {
        exoPlayer.seekToPrevious()
        Log.d(TAG, "Previous media will play")
    }

    fun seekToPosition(position: Long) {
        if (position < 0) {
            Log.d(TAG, "seekToPosition: incorrect value")
            return
        }
        exoPlayer.seekTo(position)
        Log.d(TAG, "Media position changed to $position ms")
    }

    fun changeToMediaAt(index: Int) {
        if (index == Int.MIN_VALUE || index < 0) {
            Log.e(TAG, "changeToMediaAt: incorrect value")
            return
        }
        exoPlayer.seekTo(index, 0L)
        Log.d(TAG, "Media item changed to index $index")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand received")
        intent?.getStringExtra(STREAM_URI_ARG)?.let {
            Log.d(TAG, "onStartCommand: received Stream uri $it")
            serviceScope.launch {
                val result = musicRepository.loadMediaFromUri(it)
                result.onFailure { error ->
                    Log.e(TAG, "onStartCommand: Error on single stream $error")
                }
                result.onSuccess {
                    withContext(Dispatchers.Main) {
                        exoPlayer.setMediaItem(it)
                    }
                }
            }
            exoPlayer.prepare()
            playPause()
        }

        intent?.getStringExtra(PLAYLIST_URI_ARG)?.let {
            Log.d(TAG, "onStartCommand: received Playlist uri $it")
            serviceScope.launch {
                val results = musicRepository.loadPlaylistFromUri(it)
                results.forEach { result ->
                    result.onFailure { error ->
                        Log.e(TAG, "onStartCommand: Error on stream from playlist $error")
                    }
                    result.onSuccess {
                        withContext(Dispatchers.Main) {
                            exoPlayer.addMediaItem(it)
                        }
                    }
                }
            }
            exoPlayer.prepare()
            playPause()
        }

        intent?.getIntExtra(ACTION_ARG, PlayerCmd.UNSET.ordinal)?.let {
            val cmd = PlayerCmd.fromInt(it)
            when (cmd) {
                PlayerCmd.PLAY_PAUSE -> playPause()
                PlayerCmd.STOP -> stopPlayback()
                PlayerCmd.SEEK_TO -> {
                    val position = intent.getStringExtra(POSITION_ARG)
                    if (position == null) {
                        Log.e(TAG, "onStartCommand: invalid position value")
                    } else seekToPosition(position.toLong())
                }

                PlayerCmd.UNSET -> {
                    /* no-op */
                }

                PlayerCmd.NEXT -> seekToNext()
                PlayerCmd.PREV -> seekToPrevious()
            }
        }

        return START_STICKY
    }

    private lateinit var mediaSessionForService: MediaSession

    // Create your Player and MediaSession in onCreate
    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()

        musicRepository = NewPipeMusicRepository()

        // Create an Intent for the activity you want to start.
        val resultIntent = Intent(this, MainActivity::class.java)

        // Create the TaskStackBuilder.
        val resultPendingIntent: PendingIntent = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack.
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack.
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!
        }

        mediaSessionForService = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(resultPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSessionForService
    }

    override fun onDestroy() {
        mediaSessionForService.release()
        exoPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }
}