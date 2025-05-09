package dev.crazo7924.onlymusic.db

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "songId"
    )
    val songs: List<Song>
)