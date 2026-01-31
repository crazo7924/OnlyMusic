package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.ui.components.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.toMediaListItem
import java.net.URI
import java.util.UUID

class CachingMusicRepository(
    private val decorated: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
) : MusicRepository by decorated {

    override suspend fun search(query: String): List<Result<MediaListItem>> {
        val cachedResults = playlistDao.getLikedSongs().songs.map { 
            Result.success(it.toMediaListItem())
        }
        if (cachedResults.isNotEmpty()) {
            return cachedResults
        }

        val networkResults = decorated.search(query)
        networkResults.forEach { result ->
            result.getOrNull()?.let { mediaListItem ->
                songDao.insertSong(
                    Song(
                        songId = UUID.randomUUID(),
                        title = mediaListItem.title ?: "",
                        uri = mediaListItem.mediaUri?.let { URI.create(it) } ?: URI(""),
                        artworkUri = mediaListItem.thumbnailUri?.let { URI.create(it) },
                        duration = mediaListItem.duration ?: 0L
                    )
                )
            }
        }
        return networkResults
    }
}