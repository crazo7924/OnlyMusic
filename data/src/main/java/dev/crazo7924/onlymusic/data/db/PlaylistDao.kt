package dev.crazo7924.onlymusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.util.UUID

@Dao
interface PlaylistDao {
    @Transaction
    @Query("Select * from Playlist where name = 'liked' and playlistType = 'INTERNAL'")
    fun getLikedSongs(): PlaylistWithSongs?

    @Transaction
    @Query("Select * from Playlist where name = 'recent' and playlistType = 'INTERNAL'")
    fun getRecentSongs(): PlaylistWithSongs?

    @Query("Select playlistId from Playlist where name = 'recent' and playlistType = 'INTERNAL'")
    fun getRecentPlaylistId(): UUID

    @Query("Select playlistId from Playlist where name = 'liked' and playlistType = 'INTERNAL'")
    fun getLikedPlaylistId(): UUID

    @Query("Select * from Playlist where name = :name and playlistType = 'LOCAL'")
    fun findPlaylistsByName(name: String): List<Playlist>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongToPlaylist(playlistSongsCrossRef: PlaylistSongsCrossRef)

    @Transaction
    @Query("insert into Playlist (name, playlistType) values (:name, 'LOCAL')")
    fun createPlaylist(name: String)

    @Transaction
    @Query("Select * from Playlist where playlistId = :id")
    fun getPlaylist(id: UUID): PlaylistWithSongs?
}