package dev.crazo7924.onlymusic.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import java.util.UUID

@Dao
interface PlaylistDao {
    @Transaction
    @Query("Select * from Playlist where name = 'liked'")
    fun getLikedSongs(): PlaylistWithSongs

    @Transaction
    @Query("Select * from Playlist where name = 'recent'")
    fun getRecentSongs(): PlaylistWithSongs

    @Transaction
    @Query("Select * from Playlist where id = :id")
    fun getPlaylist(id: UUID): PlaylistWithSongs
}