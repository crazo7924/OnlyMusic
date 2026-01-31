package dev.crazo7924.onlymusic.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import dev.crazo7924.onlymusic.data.db.OnlyMusicDatabase
import dev.crazo7924.onlymusic.data.repository.CachingMusicRepository
import dev.crazo7924.onlymusic.player.MediaControllerManager
import dev.crazo7924.onlymusic.player.PlayerViewModel
import dev.crazo7924.onlymusic.player.PlayerViewModelFactory
import dev.crazo7924.onlymusic.data.repository.NewPipeMusicRepository
import dev.crazo7924.onlymusic.search.SearchViewModel
import dev.crazo7924.onlymusic.search.SearchViewModelFactory
import dev.crazo7924.onlymusic.theme.OnlyMusicTheme

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            OnlyMusicDatabase::class.java, "only-music-database"
        ).build()
    }
    private val playlistDao by lazy { db.playlistDao() }
    private val songDao by lazy { db.songDao() }

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            musicRepository = CachingMusicRepository(
                decorated = NewPipeMusicRepository(),
                playlistDao = playlistDao,
                songDao = songDao
            ), minQueryLength = 2
        )
    }

    private val playerViewModel: PlayerViewModel by viewModels { PlayerViewModelFactory() }
    private lateinit var mediaControllerManager: MediaControllerManager

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onStart() {
        super.onStart()
        mediaControllerManager.initialize()
    }

    override fun onStop() {
        super.onStop()
        // Playback continues in background, so we don't release here.
    }

    override fun onDestroy() {
        mediaControllerManager.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        mediaControllerManager = MediaControllerManager(this, playerViewModel, lifecycleScope)

        setContent {
            OnlyMusicTheme {
                MainScreen(
                    playerViewModel = playerViewModel,
                    searchViewModel = searchViewModel,
                    mediaControllerManager = mediaControllerManager
                )
            }
        }
    }
}
