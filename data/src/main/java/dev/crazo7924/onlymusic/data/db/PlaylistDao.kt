/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PlaylistDao {
    @Transaction
    @Query("Select * from Playlist where name = 'liked' and playlistType = 'INTERNAL'")
    fun getLikedSongs(): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("Select * from Playlist where name = 'recent' and playlistType = 'INTERNAL'")
    fun getRecentSongs(): Flow<PlaylistWithSongs?>

    @Query("Select playlistId from Playlist where name = 'recent' and playlistType = 'INTERNAL'")
    suspend fun getRecentPlaylistId(): UUID?

    @Query("Select playlistId from Playlist where name = 'liked' and playlistType = 'INTERNAL'")
    suspend fun getLikedPlaylistId(): UUID?

    @Transaction
    @Query("Select * from Playlist where name = 'queue' and playlistType = 'INTERNAL'")
    suspend fun getQueueSongs(): PlaylistWithSongs?

    @Query("Select playlistId from Playlist where name = 'queue' and playlistType = 'INTERNAL'")
    suspend fun getQueuePlaylistId(): UUID?

    @Query("Delete from PlaylistSongsCrossRef where playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: UUID)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongToPlaylist(playlistSongsCrossRef: PlaylistSongsCrossRef)

    @Transaction
    @Query("Select * from Playlist where playlistId = :id")
    fun getPlaylist(id: UUID): Flow<PlaylistWithSongs?>
}
