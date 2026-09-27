/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.crazo7924.onlymusic.data.repository.CachingMusicRepository
import dev.crazo7924.onlymusic.data.repository.MediaRepository
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.data.repository.NewPipeMusicRepository
import dev.crazo7924.onlymusic.data.repository.QueueRepository
import dev.crazo7924.onlymusic.data.repository.RecentsRepository
import dev.crazo7924.onlymusic.data.repository.SearchHistoryRepository
import dev.crazo7924.onlymusic.data.repository.SearchRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        newPipeMusicRepository: NewPipeMusicRepository,
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        newPipeMusicRepository: NewPipeMusicRepository,
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        cachingMusicRepository: CachingMusicRepository,
    ): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindRecentsRepository(
        cachingMusicRepository: CachingMusicRepository,
    ): RecentsRepository

    @Binds
    @Singleton
    abstract fun bindQueueRepository(
        cachingMusicRepository: CachingMusicRepository,
    ): QueueRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        cachingMusicRepository: CachingMusicRepository,
    ): MusicRepository
}
