package dev.crazo7924.onlymusic.repository

import dev.crazo7924.onlymusic.MediaListItem

interface MusicRepository {
    suspend fun loadMediaUri(uri: String?): Result<MediaListItem>
    suspend fun loadPlaylistUri(uri: String?): List<Result<MediaListItem>>

    suspend fun loadAutoPlaylistUri(uri: String?): List<Result<MediaListItem>>
    suspend fun search(query: String): List<Result<MediaListItem>>
}