package dev.crazo7924.onlymusic.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation
import java.util.UUID

@Entity(primaryKeys = ["playlistId", "songId"], indices = [Index(value = ["songId"])])
data class PlaylistSongsCrossRef(
    val playlistId: UUID,
    val songId: UUID
)

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(PlaylistSongsCrossRef::class),
        entity = Song::class
    )
    val songs: List<SongWithArtists>
)
