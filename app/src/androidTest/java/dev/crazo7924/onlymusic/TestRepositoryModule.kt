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
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class] // Replace with your actual production RepositoryModule class name
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideMusicRepository(): MusicRepository = mockk(relaxed = true)
}