package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.ui.components.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistSongsCrossRef
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.UUID

class CachingMusicRepository(
    private val remoteRepository: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
) : MusicRepository by remoteRepository {

    override suspend fun search(query: String): List<Result<MediaListItem>> = coroutineScope {

        val cachedResults = async(Dispatchers.IO) {
            playlistDao.getRecentSongs()?.songs?.map {
                Result.success(it.toMediaListItem())
            } ?: emptyList()
        }

        val remoteResults = remoteRepository.search(query)
        remoteResults.forEach { result ->
            result.getOrNull()?.let { mediaListItem ->
                val song = Song(
                    songId = UUID.randomUUID(),
                    title = mediaListItem.title ?: "",
                    uri = mediaListItem.mediaUri?.let { URI.create(it) } ?: URI(""),
                    artworkUri = mediaListItem.thumbnailUri?.let { URI.create(it) },
                    duration = mediaListItem.duration ?: 0L
                )
                songDao.insertSong(song)

                val recentPlaylistId = async(Dispatchers.IO) {
                    playlistDao.getRecentPlaylistId()
                }

                withContext(Dispatchers.IO) {
                    playlistDao.insertSongToPlaylist(
                        PlaylistSongsCrossRef(
                            playlistId = recentPlaylistId.await(),
                            songId = song.songId
                        )
                    )
                }
            }
        }
        return@coroutineScope remoteResults + cachedResults.await()
    }
}