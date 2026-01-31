package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.ui.components.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.toMediaListItem

class LocalMusicRepository(private val playlistDao: PlaylistDao) : MusicRepository {
    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> {
        return Result.failure(
            Exception("App is in offline mode")
        )
    }

    override suspend fun loadPlaylistUri(uri: String?): List<Result<MediaListItem>> {
        return listOf(Result.failure(
            Exception("App is in offline mode")
        ))
    }

    override suspend fun loadAutoPlaylistUri(uri: String?): List<Result<MediaListItem>> {
        return listOf(Result.failure(
            Exception("App is in offline mode")
        ))
    }

    override suspend fun search(query: String): List<Result<MediaListItem>> {
        return playlistDao.getLikedSongs().songs.map { 
            Result.success(it.toMediaListItem())
        }
    }
}