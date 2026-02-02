package dev.crazo7924.onlymusic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI
import java.util.UUID

/**
 * Represents a collections of [Song]s.
 *
 * It has it's own [playlistId] and a [name] to distinguish from others.
 *
 * The [playlistType] could be
 * an [PlaylistType.ALBUM],
 * a [PlaylistType.PUBLIC],
 * a [PlaylistType.LOCAL] or
 * a [PlaylistType.INTERNAL] one.
 **/
@Entity
data class Playlist(
    @PrimaryKey val playlistId: UUID = UUID.randomUUID(),
    val name: String,
    /** This is null iff it is locally stored */
    val uri: URI?,
    val playlistType: PlaylistType,
)

/**
 * Categories for a [Playlist]
 * */
enum class PlaylistType {
    ALBUM,
    PUBLIC,
    LOCAL,

    INTERNAL
}