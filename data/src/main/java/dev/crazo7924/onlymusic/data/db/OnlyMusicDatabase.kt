package dev.crazo7924.onlymusic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.net.URI
import java.util.UUID

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        Playlist::class,
        Song::class,
        Artist::class,
        PlaylistSongsCrossRef::class
    ]
)
@TypeConverters(Converters::class)
abstract class OnlyMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
}

val initPlaylistCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO Playlist (playlistId, name, playlistType) VALUES (?, 'liked', 'INTERNAL')",
            arrayOf(UUID.randomUUID())
        )
        db.execSQL("INSERT INTO Playlist (playlistId, name, playlistType) VALUES (?, 'recent', 'INTERNAL')",
            arrayOf(UUID.randomUUID())
        )
    }
}

class Converters {
    @TypeConverter
    fun uriToString(uri: URI?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun stringToURI(uri: String?): URI? {
        return uri?.let { URI(it) }
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun stringToUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }
}