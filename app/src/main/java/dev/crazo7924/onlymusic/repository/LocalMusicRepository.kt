package dev.crazo7924.onlymusic.repository

import dev.crazo7924.onlymusic.MediaListItem

class LocalMusicRepository : MusicRepository {
    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> {
        TODO("Not yet implemented")
    }

    override suspend fun loadPlaylistUri(uri: String?): List<Result<MediaListItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadAutoPlaylistUri(uri: String?): List<Result<MediaListItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String): List<Result<MediaListItem>> {
        TODO("Not yet implemented")
    }
}