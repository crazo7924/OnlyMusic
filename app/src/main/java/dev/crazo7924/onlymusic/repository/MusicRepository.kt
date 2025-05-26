package dev.crazo7924.onlymusic.repository

import androidx.media3.common.MediaItem

interface MusicRepository {
    suspend fun loadMediaFromUri(uri: String?): Result<MediaItem>
    suspend fun loadPlaylistFromUri(uri: String?): List<Result<MediaItem>>
}