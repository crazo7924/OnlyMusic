package dev.crazo7924.onlymusic.data.repository

import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.PlaylistSongsCrossRef
import dev.crazo7924.onlymusic.data.db.Song
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.toMediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URI
import java.util.UUID

class CachingMusicRepository(
    private val remoteRepository: MusicRepository,
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao,
) : MusicRepository by remoteRepository {

    override suspend fun search(query: String): Flow<Result<MediaListItem>> = flow {

        val remoteResults = remoteRepository.search(query)

        remoteResults.collect { result ->
            emit(result)
            result.getOrNull()?.let { mediaListItem ->
                val song = Song(
                    songId = UUID.randomUUID(),
                    title = mediaListItem.title ?: "",
                    uri = mediaListItem.mediaUri?.let { URI.create(it) } ?: URI(""),
                    artworkUri = mediaListItem.thumbnailUri?.let { URI.create(it) },
                    duration = mediaListItem.duration ?: 0L
                )
                songDao.insertSong(song)

                val recentPlaylistId = playlistDao.getRecentPlaylistId()

                playlistDao.insertSongToPlaylist(
                    PlaylistSongsCrossRef(
                        playlistId = recentPlaylistId, songId = song.songId
                    )
                )
            }
        }

        playlistDao.getRecentSongs()?.songs?.forEach {
            emit(Result.success(it.toMediaListItem()))
        }
    }.flowOn(Dispatchers.IO)
}