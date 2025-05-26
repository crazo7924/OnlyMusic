package dev.crazo7924.onlymusic.repository

import androidx.media3.common.MediaItem

class LocalMusicRepository : MusicRepository {
    override suspend fun loadMediaFromUri(uri: String?): Result<MediaItem> {
        TODO("Not yet implemented")
    }

    override suspend fun loadPlaylistFromUri(uri: String?): List<Result<MediaItem>> {
        TODO("Not yet implemented")
    }
}