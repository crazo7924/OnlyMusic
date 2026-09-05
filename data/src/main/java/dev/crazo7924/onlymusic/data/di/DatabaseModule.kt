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
import dev.crazo7924.onlymusic.data.db.SearchHistoryDao
import dev.crazo7924.onlymusic.data.db.SongDao
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `search_history` (`query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`query`))"
                )
            }
        }

        return Room.databaseBuilder(
            context,
            OnlyMusicDatabase::class.java, "only-music-database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

    @Provides
    fun provideSearchHistoryDao(database: OnlyMusicDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }
}
