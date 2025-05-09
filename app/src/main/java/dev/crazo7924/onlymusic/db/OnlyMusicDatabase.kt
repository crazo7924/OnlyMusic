package dev.crazo7924.onlymusic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.net.URI
import java.util.UUID

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        Playlist::class,
        Song::class,
        Artist::class
    ]
)
@TypeConverters(Converters::class)
abstract class OnlyMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}

class Converters {
    @TypeConverter
    fun uriToString(uri: URI?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun stringToURI(uri: String?): URI? {
        return URI.create(uri)
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun stringToUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}