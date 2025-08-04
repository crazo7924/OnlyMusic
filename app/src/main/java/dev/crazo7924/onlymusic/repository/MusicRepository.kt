package dev.crazo7924.onlymusic.repository

import androidx.media3.common.MediaItem
import dev.crazo7924.onlymusic.MediaListItem

interface MusicRepository {
    suspend fun loadMediaUri(uri: String?): Result<MediaItem>
    suspend fun loadPlaylistUri(uri: String?): List<Result<MediaItem>>

    suspend fun loadAutoPlaylistUri(uri: String?): List<Result<MediaItem>>
    suspend fun search(query: String): List<Result<MediaListItem>>
}