package dev.crazo7924.onlymusic.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI
import java.util.UUID

@Entity
data class Song(
    @PrimaryKey val songId: UUID = UUID.randomUUID(),
    val title: String,
    val uri: URI,
    val artworkUri: URI?,
)

