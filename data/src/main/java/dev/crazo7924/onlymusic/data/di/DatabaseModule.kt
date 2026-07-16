/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.crazo7924.onlymusic.data.db.ArtistDao
import dev.crazo7924.onlymusic.data.db.OnlyMusicDatabase
import dev.crazo7924.onlymusic.data.db.PlaylistDao
import dev.crazo7924.onlymusic.data.db.SongDao
import dev.crazo7924.onlymusic.data.db.initPlaylistCallback
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OnlyMusicDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `SongArtistCrossRef` (`songId` TEXT NOT NULL, `artistId` TEXT NOT NULL, PRIMARY KEY(`songId`, `artistId`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_SongArtistCrossRef_artistId` ON `SongArtistCrossRef` (`artistId`)"
                )
            }
        }

        return Room.databaseBuilder(
            context,
            OnlyMusicDatabase::class.java, "only-music-database"
        )
            .addCallback(initPlaylistCallback)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    fun providePlaylistDao(database: OnlyMusicDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideSongDao(database: OnlyMusicDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideArtistDao(database: OnlyMusicDatabase): ArtistDao {
        return database.artistDao()
    }
}
