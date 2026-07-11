/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.crazo7924.onlymusic.MediaControllerManager
import dev.crazo7924.onlymusic.core.ui.theme.OnlyMusicTheme
import dev.crazo7924.onlymusic.features.player.PlayerViewModel
import dev.crazo7924.onlymusic.features.search.SearchViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val searchViewModel: SearchViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

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
        mediaControllerManager.initialize()
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
