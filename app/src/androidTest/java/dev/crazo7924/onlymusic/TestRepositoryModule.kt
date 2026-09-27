/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.crazo7924.onlymusic.data.di.RepositoryModule
import dev.crazo7924.onlymusic.data.repository.MediaRepository
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.data.repository.QueueRepository
import dev.crazo7924.onlymusic.data.repository.RecentsRepository
import dev.crazo7924.onlymusic.data.repository.SearchHistoryRepository
import dev.crazo7924.onlymusic.data.repository.SearchRepository
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideMusicRepository(): MusicRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideMediaRepository(musicRepository: MusicRepository): MediaRepository = musicRepository

    @Provides
    @Singleton
    fun provideSearchRepository(musicRepository: MusicRepository): SearchRepository = musicRepository

    @Provides
    @Singleton
    fun provideSearchHistoryRepository(musicRepository: MusicRepository): SearchHistoryRepository = musicRepository

    @Provides
    @Singleton
    fun provideRecentsRepository(musicRepository: MusicRepository): RecentsRepository = musicRepository

    @Provides
    @Singleton
    fun provideQueueRepository(musicRepository: MusicRepository): QueueRepository = musicRepository
}
