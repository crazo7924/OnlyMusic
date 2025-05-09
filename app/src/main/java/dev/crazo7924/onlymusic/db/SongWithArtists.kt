package dev.crazo7924.onlymusic.db

import androidx.room.Embedded
import androidx.room.Relation

data class SongWithArtists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "id",
        entityColumn = "artistId"
    )
    val artists: List<Artist>,
)