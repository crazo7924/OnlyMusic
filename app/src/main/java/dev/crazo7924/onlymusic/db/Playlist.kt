package dev.crazo7924.onlymusic.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URI
import java.util.UUID

/**
 * Represents a collections of [songs].
 *
 * It has it's own [id] and a [name] to distinguish from others.
 *
 * The [playlistType] could be
 *
 * an [PlaylistType.ALBUM],
 *
 * a [PlaylistType.PUBLIC] or
 *
 * a [PlaylistType.LOCAL] one.
 *
 **/
@Entity
data class Playlist(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    /** This is null iff it is locally stored */
    val uri: URI?,
    val artworkUri: URI,
    val playlistType: PlaylistType,
)

/**
 * Categories for a [Playlist]
 * */
enum class PlaylistType {
    ALBUM,
    PUBLIC,
    LOCAL
}