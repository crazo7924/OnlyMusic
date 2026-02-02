package dev.crazo7924.onlymusic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Artist(
    @PrimaryKey val artistId: UUID = UUID.randomUUID(),
    val name: String,
)
