/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.data.di.RepositoryModule
import dev.crazo7924.onlymusic.data.repository.MediaRepository
import dev.crazo7924.onlymusic.data.repository.MusicRepository
import dev.crazo7924.onlymusic.data.repository.QueueRepository
import dev.crazo7924.onlymusic.data.repository.RecentsRepository
import dev.crazo7924.onlymusic.data.repository.SavedQueueState
import dev.crazo7924.onlymusic.data.repository.SearchHistoryRepository
import dev.crazo7924.onlymusic.data.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

class FakeMusicRepository : MusicRepository {
    var loadMediaUriLambda: suspend (String?) -> Result<MediaListItem> =
        { Result.failure(Exception("Not stubbed")) }
    var loadPlaylistUriLambda: suspend (String?) -> Flow<Result<MediaListItem>> = { flowOf() }
    var loadAutoPlaylistUriLambda: suspend (String?) -> Flow<Result<MediaListItem>> = { flowOf() }
    var loadMorePlaylistItemsLambda: suspend () -> Flow<Result<MediaListItem>> = { flowOf() }
    var saveQueueLambda: suspend (List<MediaListItem>, Int, Long) -> Unit = { _, _, _ -> }
    var getSavedQueueLambda: suspend () -> SavedQueueState? = { null }
    var getRecentSongsLambda: suspend () -> Flow<List<MediaListItem>> = { flowOf(emptyList()) }
    var saveToRecentsLambda: suspend (MediaListItem) -> Unit = { }
    var searchLambda: suspend (String) -> Flow<Result<MediaListItem>> = { flowOf() }
    var getSearchSuggestionsLambda: suspend (String) -> Result<List<String>> =
        { Result.success(emptyList()) }
    var getRecentQueriesLambda: suspend () -> Flow<List<String>> = { flowOf(emptyList()) }
    var addRecentQueryLambda: suspend (String) -> Unit = { }
    var deleteRecentQueryLambda: suspend (String) -> Unit = { }

    fun resetDefaults() {
        loadMediaUriLambda = { Result.failure(Exception("Not stubbed")) }
        loadPlaylistUriLambda = { flowOf() }
        loadAutoPlaylistUriLambda = { flowOf() }
        loadMorePlaylistItemsLambda = { flowOf() }
        saveQueueLambda = { _, _, _ -> }
        getSavedQueueLambda = { null }
        getRecentSongsLambda = { flowOf(emptyList()) }
        saveToRecentsLambda = { }
        searchLambda = { flowOf() }
        getSearchSuggestionsLambda = { Result.success(emptyList()) }
        getRecentQueriesLambda = { flowOf(emptyList()) }
        addRecentQueryLambda = { }
        deleteRecentQueryLambda = { }
    }

    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> =
        loadMediaUriLambda(uri)

    override suspend fun loadPlaylistUri(uri: String?): Flow<Result<MediaListItem>> =
        loadPlaylistUriLambda(uri)

    override suspend fun loadAutoPlaylistUri(uri: String?): Flow<Result<MediaListItem>> =
        loadAutoPlaylistUriLambda(uri)

    override suspend fun loadMorePlaylistItems(): Flow<Result<MediaListItem>> =
        loadMorePlaylistItemsLambda()

    override suspend fun saveQueue(items: List<MediaListItem>, activeIndex: Int, positionMs: Long) =
        saveQueueLambda(items, activeIndex, positionMs)

    override suspend fun getSavedQueue(): SavedQueueState? = getSavedQueueLambda()

    override suspend fun getRecentSongs(): Flow<List<MediaListItem>> = getRecentSongsLambda()

    override suspend fun saveToRecents(mediaListItem: MediaListItem) =
        saveToRecentsLambda(mediaListItem)

    override suspend fun search(query: String): Flow<Result<MediaListItem>> = searchLambda(query)

    override suspend fun getSearchSuggestions(query: String): Result<List<String>> =
        getSearchSuggestionsLambda(query)

    override suspend fun getRecentQueries(): Flow<List<String>> = getRecentQueriesLambda()

    override suspend fun addRecentQuery(query: String) = addRecentQueryLambda(query)

    override suspend fun deleteRecentQuery(query: String) = deleteRecentQueryLambda(query)
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {
    val fakeMusicRepository = FakeMusicRepository()

    @Provides
    @Singleton
    fun provideMusicRepository(): MusicRepository = fakeMusicRepository

    @Provides
    @Singleton
    fun provideMediaRepository(musicRepository: MusicRepository): MediaRepository = musicRepository

    @Provides
    @Singleton
    fun provideSearchRepository(musicRepository: MusicRepository): SearchRepository = musicRepository

    @Provides
    @Singleton
    fun provideSearchHistoryRepository(musicRepository: MusicRepository): SearchHistoryRepository =
        musicRepository

    @Provides
    @Singleton
    fun provideRecentsRepository(musicRepository: MusicRepository): RecentsRepository = musicRepository

    @Provides
    @Singleton
    fun provideQueueRepository(musicRepository: MusicRepository): QueueRepository = musicRepository
}
