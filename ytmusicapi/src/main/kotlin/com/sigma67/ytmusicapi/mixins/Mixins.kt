package com.sigma67.ytmusicapi.mixins

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList

/**
 * Browsing functionality mixin
 */
interface BrowsingMixin {
    fun getHome(limit: Int = 3): JsonList
    fun getExplore(): JsonDict
    fun getAlbum(browseId: String): JsonDict
    fun getArtist(channelId: String): JsonDict
    fun getPlaylist(playlistId: String, limit: Int = 100): JsonDict
    fun getWatchPlaylist(videoId: String, limit: Int = 100): JsonDict
    fun getWatchPlaylistShuffle(videoId: String, limit: Int = 50): JsonDict
}

/**
 * Search functionality mixin
 */
interface SearchMixin {
    fun search(
        query: String,
        filter: String? = null,
        scope: String? = null,
        limit: Int = 20,
        ignoreSpelling: Boolean = false,
    ): JsonList
}

/**
 * Watch/History functionality mixin
 */
interface WatchMixin {
    fun getWatchPlaylist(
        videoId: String? = null,
        playlistId: String? = null,
        limit: Int = 25,
    ): JsonDict

    fun getWatchPlaylistShuffle(
        videoId: String? = null,
        playlistId: String? = null,
        limit: Int = 25,
    ): JsonDict
}

/**
 * Charts functionality mixin
 */
interface ChartsMixin {
    fun getCharts(country: String = ""): JsonDict
}

/**
 * Explore functionality mixin
 */
interface ExploreMixin {
    fun getExplore(): JsonDict
    fun getExplorePlaylist(params: String): JsonDict
}

/**
 * Library functionality mixin
 */
interface LibraryMixin {
    fun getLibrarySongs(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun getLibraryPlaylist(playlistId: String, limit: Int = 100): JsonDict
    fun getLibraryAlbums(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun getLibraryArtists(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun addSongToPlaylist(playlistId: String, videoId: String): JsonDict
    fun removeSongsFromPlaylist(playlistId: String, videoIds: List<String>): JsonDict
}

/**
 * Playlists functionality mixin
 */
interface PlaylistsMixin {
    fun createPlaylist(
        title: String,
        description: String = "",
        privacy: String = "PRIVATE",
        videoIds: List<String> = emptyList(),
    ): String

    fun deletePlaylist(playlistId: String): String
    fun getPlaylist(playlistId: String, limit: Int = 100): JsonDict
    fun getPlaylistItems(
        playlistId: String,
        limit: Int = 100,
        relatedBrowseId: String? = null,
    ): JsonList

    fun addPlaylistItems(playlistId: String, videoIds: List<String>): JsonDict
    fun removePlaylistItems(playlistId: String, videoIds: List<String>): JsonDict
    fun updatePlaylistMetadata(
        playlistId: String,
        updates: JsonDict,
    ): JsonDict
}

/**
 * Podcasts functionality mixin
 */
interface PodcastsMixin {
    fun getPodcast(browseId: String, limit: Int = 25): JsonDict
    fun getPodcastEpisodes(browseId: String, limit: Int = 25): JsonList
    fun subscribeToPodcast(browseId: String): JsonDict
    fun unsubscribeFromPodcast(browseId: String): JsonDict
}

/**
 * Uploads functionality mixin
 */
interface UploadsMixin {
    fun getLibraryUploadSongs(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun getLibraryUploadAlbums(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun getLibraryUploadArtists(limit: Int = 25, validateTokens: Boolean = false): JsonList
    fun deleteUploadEntity(entityType: String, entityId: String): JsonDict
}
