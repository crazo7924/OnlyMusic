/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        return Room.databaseBuilder(
            context,
            OnlyMusicDatabase::class.java, "only-music-database"
        )
            .addCallback(initPlaylistCallback)
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
}
