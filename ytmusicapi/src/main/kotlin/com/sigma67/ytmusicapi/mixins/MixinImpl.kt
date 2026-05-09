package com.sigma67.ytmusicapi.mixins

import com.sigma67.ytmusicapi.*
import com.sigma67.ytmusicapi.parsers.BrowsingParser
import com.sigma67.ytmusicapi.parsers.ExploreParser
import com.sigma67.ytmusicapi.parsers.LibraryParser
import com.sigma67.ytmusicapi.parsers.PlaylistsParser
import com.sigma67.ytmusicapi.parsers.WatchParser
import com.sigma67.ytmusicapi.parsers.parseMixedContent

/**
 * Browsing mixin implementation
 */
class BrowsingMixinImpl : YTMusicBase(), BrowsingMixin {
    override fun getHome(limit: Int): JsonList {
        val body = mapOf("browseId" to HOME_BROWSE_ID)
        val response = sendRequest(BROWSE_ENDPOINT, body)

        val results = navigatePath(response, SINGLE_COLUMN_TAB + SECTION_LIST, true) as? JsonList
            ?: emptyList()
        val home = parseMixedContent(results)

        // Handle continuations if needed
        val sectionList = navigatePath(
            response, SINGLE_COLUMN_TAB + listOf("sectionListRenderer"), true
        ) as? JsonDict
        if (sectionList != null && sectionList.containsKey("continuations")) {
            // TODO: Implement continuations handling
        }

        return home.take(limit)
    }

    override fun getExplore(): JsonDict {
        val body = mapOf("browseId" to EXPLORE_BROWSE_ID)
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return BrowsingParser.parseArtistContent(response)
    }

    override fun getAlbum(browseId: String): JsonDict {
        val body = mapOf("browseId" to browseId)
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return BrowsingParser.parseAlbum(response)
    }

    override fun getArtist(channelId: String): JsonDict {
        val body = mapOf("browseId" to channelId)
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return BrowsingParser.parseArtist(response)
    }

    override fun getPlaylist(playlistId: String, limit: Int): JsonDict {
        val body = mapOf("browseId" to "VL$playlistId")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val playlist = PlaylistsParser.parsePlaylist(response).toMutableMap()

        // Apply limit to tracks if specified
        if (limit > 0) {
            val tracks = playlist["tracks"] as? JsonList
            if (tracks != null) {
                playlist["tracks"] = tracks.take(limit)
            }
        }

        return playlist
    }

    override fun getWatchPlaylist(videoId: String, limit: Int): JsonDict {
        val body = mapOf(
            "enablePersistentPlaylistPanel" to true,
            "isAudioOnly" to true,
            "tunerSettingValue" to "AUTOMIX_SETTING_NORMAL",
            "videoId" to videoId
        )
        val response = sendRequest(NEXT_ENDPOINT, body)
        return WatchParser.parseWatchPlaylist(response)
    }

    override fun getWatchPlaylistShuffle(videoId: String, limit: Int): JsonDict {
        val body = mapOf(
            "enablePersistentPlaylistPanel" to true,
            "isAudioOnly" to true,
            "tunerSettingValue" to "AUTOMIX_SETTING_NORMAL",
            "videoId" to videoId,
            "shuffle" to true
        )
        val response = sendRequest(NEXT_ENDPOINT, body)
        return WatchParser.parseWatchPlaylist(response)
    }
}

/**
 * Search mixin implementation
 */
class SearchMixinImpl : YTMusicBase(), SearchMixin {
    override fun search(
        query: String,
        filter: String?,
        scope: String?,
        limit: Int,
        ignoreSpelling: Boolean,
    ): JsonList {
        val body = mutableMapOf<String, Any>(
            "query" to query, "params" to getSearchParams(filter, scope)
        )

        if (ignoreSpelling) {
            body["ignoreSpelling"] = true
        }

        val response = sendRequest(SEARCH_ENDPOINT, body)

        val results = navigatePath(
            response, listOf(
                "contents",
                "tabbedSearchResultsRenderer",
                "tabs",
                0,
                "tabRenderer",
                "content",
                "sectionListRenderer",
                "contents"
            ), true
        ) as? JsonList ?: emptyList()

        val parsedResults = mutableListOf<JsonDict>()
        for (result in results) {
            val resultMap = result as? JsonDict ?: continue
            val musicShelfRenderer =
                navigatePath(resultMap, listOf("musicShelfRenderer"), true) as? JsonDict
            if (musicShelfRenderer != null) {
                val contents =
                    navigatePath(musicShelfRenderer, listOf("contents"), true) as? JsonList
                        ?: continue
                for (item in contents) {
                    val itemMap = item ?: continue
                    // Parse based on item type - simplified for now
                    val parsedItem = mutableMapOf<String, Any?>()
                    parsedItem["title"] = navigatePath(itemMap, TITLE_TEXT, true)
                    parsedItem["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)
                    parsedResults.add(parsedItem)
                }
            }
        }

        return parsedResults.take(limit)
    }

    private fun getSearchParams(filter: String?, scope: String?): String {
        return when (filter) {
            "songs" -> "EgWKAQIIAWoKEAkQAxAKEAkQBQ%3D%3D"
            "videos" -> "EgWKAQIQAWoKEAkQAxAKEAkQBQ%3D%3D"
            "albums" -> "EgWKAQIYAWoKEAkQAxAKEAkQBQ%3D%3D"
            "artists" -> "EgWKAQIgAWoKEAkQAxAKEAkQBQ%3D%3D"
            "playlists" -> "EgWKAQIoAWoKEAkQAxAKEAkQBQ%3D%3D"
            else -> ""
        }
    }
}

/**
 * Watch mixin implementation
 */
class WatchMixinImpl : YTMusicBase(), WatchMixin {
    override fun getWatchPlaylist(videoId: String?, playlistId: String?, limit: Int): JsonDict {
        val body = mutableMapOf<String, Any>(
            "enablePersistentPlaylistPanel" to true,
            "isAudioOnly" to true,
            "tunerSettingValue" to "AUTOMIX_SETTING_NORMAL"
        )

        if (videoId != null) {
            body["videoId"] = videoId
        }
        if (playlistId != null) {
            body["playlistId"] = playlistId
        }

        val response = sendRequest(NEXT_ENDPOINT, body)
        return WatchParser.parseWatchPlaylist(response)
    }

    override fun getWatchPlaylistShuffle(
        videoId: String?,
        playlistId: String?,
        limit: Int,
    ): JsonDict {
        val body = mutableMapOf<String, Any>(
            "enablePersistentPlaylistPanel" to true,
            "isAudioOnly" to true,
            "tunerSettingValue" to "AUTOMIX_SETTING_NORMAL",
            "shuffle" to true
        )

        if (videoId != null) {
            body["videoId"] = videoId
        }
        if (playlistId != null) {
            body["playlistId"] = playlistId
        }

        val response = sendRequest(NEXT_ENDPOINT, body)
        return WatchParser.parseWatchPlaylist(response)
    }
}

/**
 * Charts mixin implementation
 */
class ChartsMixinImpl : YTMusicBase(), ChartsMixin {
    override fun getCharts(country: String): JsonDict {
        val body = mapOf("browseId" to CHARTS_BROWSE_ID)
        val response = sendRequest(BROWSE_ENDPOINT, body)

        // Use the explore parser to parse charts
        return ExploreParser.parseCharts(response)
    }
}

/**
 * Explore mixin implementation
 */
class ExploreMixinImpl : YTMusicBase(), ExploreMixin {
    override fun getExplore(): JsonDict {
        val body = mapOf("browseId" to EXPLORE_BROWSE_ID)
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return ExploreParser.parseExplore(response)
    }

    override fun getExplorePlaylist(params: String): JsonDict {
        val body = mapOf(
            "browseId" to EXPLORE_BROWSE_ID, "params" to params
        )
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return PlaylistsParser.parsePlaylist(response)
    }
}

/**
 * Library mixin implementation
 */
class LibraryMixinImpl : YTMusicBase(), LibraryMixin {
    override fun getLibrarySongs(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_liked_songs")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val songs =
            LibraryParser.parseLikedSongs(response)["tracks"] as? JsonList
                ?: emptyList()
        return songs.take(limit)
    }

    override fun getLibraryPlaylist(playlistId: String, limit: Int): JsonDict {
        checkAuth()
        return getPlaylist(playlistId, limit)
    }

    override fun getLibraryAlbums(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_liked_albums")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return LibraryParser.parseLibraryAlbums(
            navigatePath(response, SINGLE_COLUMN_TAB + SECTION_LIST, true) as? JsonList
                ?: emptyList()
        ).take(limit)
    }

    override fun getLibraryArtists(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_library_corpus_track_artists")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return LibraryParser.parseLibraryArtists(
            navigatePath(response, SINGLE_COLUMN_TAB + SECTION_LIST, true) as? JsonList
                ?: emptyList()
        ).take(limit)
    }

    override fun addSongToPlaylist(playlistId: String, videoId: String): JsonDict {
        checkAuth()
        val body = mapOf(
            "playlistId" to playlistId, "actions" to listOf(
                mapOf(
                    "addedVideoId" to videoId, "action" to "ACTION_ADD_VIDEO"
                )
            )
        )
        return sendRequest(ADD_PLAYLIST_ITEMS_ENDPOINT, body)
    }

    override fun removeSongsFromPlaylist(playlistId: String, videoIds: List<String>): JsonDict {
        checkAuth()
        val body = mapOf(
            "playlistId" to playlistId, "actions" to videoIds.map { videoId ->
                mapOf(
                    "removedVideoId" to videoId, "action" to "ACTION_REMOVE_VIDEO"
                )
            })
        return sendRequest(REMOVE_PLAYLIST_ITEMS_ENDPOINT, body)
    }
}

/**
 * Playlists mixin implementation
 */
class PlaylistsMixinImpl : YTMusicBase(), PlaylistsMixin {
    override fun createPlaylist(
        title: String,
        description: String,
        privacy: String,
        videoIds: List<String>,
    ): String {
        checkAuth()
        val body = mapOf(
            "title" to title, "description" to description, "privacyStatus" to privacy.uppercase()
        )
        val response = sendRequest(CREATE_PLAYLIST_ENDPOINT, body)

        val playlistId = navigatePath(response, listOf("playlistId"), true) as? String ?: ""

        if (videoIds.isNotEmpty()) {
            addPlaylistItems(playlistId, videoIds)
        }

        return playlistId
    }

    override fun deletePlaylist(playlistId: String): String {
        checkAuth()
        val body = mapOf("playlistId" to playlistId)
        sendRequest(DELETE_PLAYLIST_ENDPOINT, body)
        return playlistId
    }

    override fun getPlaylist(playlistId: String, limit: Int): JsonDict {
        val body = mapOf("browseId" to "VL$playlistId")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val playlist = PlaylistsParser.parsePlaylist(response).toMutableMap()

        if (limit > 0) {
            val tracks = playlist["tracks"] as? JsonList
            if (tracks != null) {
                playlist["tracks"] = tracks.take(limit)
            }
        }

        return playlist
    }

    override fun getPlaylistItems(
        playlistId: String,
        limit: Int,
        relatedBrowseId: String?,
    ): JsonList {
        val playlist = getPlaylist(playlistId, limit)
        return playlist["tracks"] as? JsonList ?: emptyList()
    }

    override fun addPlaylistItems(playlistId: String, videoIds: List<String>): JsonDict {
        checkAuth()
        val body = mapOf(
            "playlistId" to playlistId, "actions" to videoIds.map { videoId ->
                mapOf(
                    "addedVideoId" to videoId, "action" to "ACTION_ADD_VIDEO"
                )
            })
        return sendRequest(ADD_PLAYLIST_ITEMS_ENDPOINT, body)
    }

    override fun removePlaylistItems(playlistId: String, videoIds: List<String>): JsonDict {
        checkAuth()
        val body = mapOf(
            "playlistId" to playlistId, "actions" to videoIds.map { videoId ->
                mapOf(
                    "removedVideoId" to videoId, "action" to "ACTION_REMOVE_VIDEO"
                )
            })
        return sendRequest(REMOVE_PLAYLIST_ITEMS_ENDPOINT, body)
    }

    override fun updatePlaylistMetadata(playlistId: String, updates: JsonDict): JsonDict {
        checkAuth()
        val body = mapOf(
            "playlistId" to playlistId
        ) + updates
        return sendRequest(EDIT_PLAYLIST_ENDPOINT, body)
    }
}

/**
 * Podcasts mixin implementation
 */
class PodcastsMixinImpl : YTMusicBase(), PodcastsMixin {
    override fun getPodcast(browseId: String, limit: Int): JsonDict {
        val body = mapOf("browseId" to browseId)
        val response = sendRequest(BROWSE_ENDPOINT, body)
        return com.sigma67.ytmusicapi.parsers.PodcastsParser.parsePodcastBrowse(response)
    }

    override fun getPodcastEpisodes(browseId: String, limit: Int): JsonList {
        val podcast = getPodcast(browseId, limit)
        return podcast["episodes"] as? JsonList ?: emptyList()
    }

    override fun subscribeToPodcast(browseId: String): JsonDict {
        checkAuth()
        val body = mapOf(
            "channelIds" to listOf(browseId), "params" to "EgIIAhgA"
        )
        return sendRequest(SUBSCRIBE_ENDPOINT, body)
    }

    override fun unsubscribeFromPodcast(browseId: String): JsonDict {
        checkAuth()
        val body = mapOf(
            "channelId" to browseId, "params" to "CgIIAhgA"
        )
        return sendRequest(UNSUBSCRIBE_ENDPOINT, body)
    }
}

/**
 * Uploads mixin implementation
 */
class UploadsMixinImpl : YTMusicBase(), UploadsMixin {
    override fun getLibraryUploadSongs(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_library_privately_owned_tracks")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val contents = navigatePath(
            response, SINGLE_COLUMN_TAB + SECTION_LIST_ITEM + MUSIC_SHELF + listOf("contents"), true
        ) as? JsonList ?: emptyList()
        return com.sigma67.ytmusicapi.parsers.UploadsParser.parseUploads(contents).take(limit)
    }

    override fun getLibraryUploadAlbums(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_library_privately_owned_releases")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val contents = navigatePath(
            response, SINGLE_COLUMN_TAB + SECTION_LIST_ITEM + MUSIC_SHELF + listOf("contents"), true
        ) as? JsonList ?: emptyList()
        return com.sigma67.ytmusicapi.parsers.AlbumsParser.parseAlbums(contents).take(limit)
    }

    override fun getLibraryUploadArtists(limit: Int, validateTokens: Boolean): JsonList {
        checkAuth()
        val body = mapOf("browseId" to "FEmusic_library_privately_owned_artists")
        val response = sendRequest(BROWSE_ENDPOINT, body)
        val contents = navigatePath(
            response, SINGLE_COLUMN_TAB + SECTION_LIST_ITEM + MUSIC_SHELF + listOf("contents"), true
        ) as? JsonList ?: emptyList()
        return contents.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                val artist = mutableMapOf<String, Any?>()
                artist["name"] = navigatePath(itemMap, TITLE_TEXT, true)
                artist["browseId"] = navigatePath(itemMap, NAVIGATION_BROWSE_ID, true)
                artist["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)
                artist
            } catch (e: Exception) {
                null
            }
        }.take(limit)
    }

    override fun deleteUploadEntity(entityType: String, entityId: String): JsonDict {
        checkAuth()
        // This would require additional implementation for the delete endpoint
        // For now, return empty map
        return emptyMap()
    }
}
