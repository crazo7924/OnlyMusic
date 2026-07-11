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
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.data.repository.NewPipeMusicRepository
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    @RemoteRepository
    abstract fun bindRemoteMusicRepository(
        newPipeMusicRepository: NewPipeMusicRepository
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        cachingMusicRepository: CachingMusicRepository
    ): MusicRepository
}
