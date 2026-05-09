package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Library parser implementation
 */

object LibraryParser {
    fun parseLibrary(data: JsonDict): JsonDict {
        val library = mutableMapOf<String, Any?>()

        val contents = navigatePath(
            data,
            listOf("contents", "singleColumnBrowseResultsRenderer", "tabs"),
            true
        ) as? JsonList
        if (contents != null) {
            for (tab in contents) {
                val tabMap = tab as? JsonDict ?: continue
                val title = navigatePath(tabMap, listOf("tabRenderer", "title"), true) as? String
                    ?: continue

                val tabContents = navigatePath(
                    tabMap,
                    listOf("tabRenderer", "content", "sectionListRenderer", "contents"),
                    true
                ) as? JsonList ?: continue

                when {
                    title.contains("Songs", ignoreCase = true) -> {
                        library["songs"] = parseLibrarySongs(tabContents)
                    }

                    title.contains("Albums", ignoreCase = true) -> {
                        library["albums"] = parseLibraryAlbums(tabContents)
                    }

                    title.contains("Artists", ignoreCase = true) -> {
                        library["artists"] = parseLibraryArtists(tabContents)
                    }

                    title.contains("Playlists", ignoreCase = true) -> {
                        library["playlists"] = parseLibraryPlaylists(tabContents)
                    }

                    title.contains("Subscriptions", ignoreCase = true) -> {
                        library["subscriptions"] = parseLibrarySubscriptions(tabContents)
                    }
                }
            }
        }

        return library
    }

    fun parseLibrarySongs(data: JsonList): JsonList {
        return data.flatMap { section ->
            val sectionMap = section as? JsonDict ?: return@flatMap emptyList()
            val shelfRenderer =
                navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    SongsParser.parseSong(itemMap)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun parseLibraryAlbums(data: JsonList): JsonList {
        return data.flatMap { section ->
            val sectionMap = section as? JsonDict ?: return@flatMap emptyList()
            val shelfRenderer =
                navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    AlbumsParser.parseAlbum(itemMap)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun parseLibraryArtists(data: JsonList): JsonList {
        return data.flatMap { section ->
            val sectionMap = section as? JsonDict ?: return@flatMap emptyList()
            val shelfRenderer =
                navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    val artist = mutableMapOf<String, Any?>()
                    artist["name"] = navigatePath(itemMap, TITLE_TEXT, true)
                    artist["browseId"] = navigatePath(itemMap, NAVIGATION_BROWSE_ID, true)
                    artist["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)

                    val subtitle = navigatePath(
                        itemMap,
                        listOf("subtitle", "runs", 0, "text"),
                        true
                    ) as? String
                    artist["subscribers"] = subtitle?.split(" ")?.get(0)

                    artist
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun parseLibraryPlaylists(data: JsonList): JsonList {
        return data.flatMap { section ->
            val sectionMap = section as? JsonDict ?: return@flatMap emptyList()
            val shelfRenderer =
                navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    val playlist = mutableMapOf<String, Any?>()
                    playlist["title"] = navigatePath(itemMap, TITLE_TEXT, true)
                    playlist["playlistId"] = navigatePath(
                        itemMap,
                        listOf(
                            "overlay",
                            "musicItemThumbnailOverlayRenderer",
                            "content",
                            "musicPlayButtonRenderer",
                            "playNavigationEndpoint",
                            "watchPlaylistEndpoint",
                            "playlistId"
                        ),
                        true
                    )
                    playlist["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)

                    val subtitleRuns =
                        navigatePath(itemMap, listOf("subtitle", "runs"), true) as? JsonList
                    if (!subtitleRuns.isNullOrEmpty()) {
                        playlist["author"] = subtitleRuns[0]["text"]
                        if (subtitleRuns.size >= 3) {
                            playlist["count"] = subtitleRuns[2]["text"]
                        }
                    }

                    playlist
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun parseLibrarySubscriptions(data: JsonList): JsonList {
        return data.flatMap { section ->
            val sectionMap = section as? JsonDict ?: return@flatMap emptyList()
            val shelfRenderer =
                navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    val subscription = mutableMapOf<String, Any?>()
                    subscription["name"] = navigatePath(itemMap, TITLE_TEXT, true)
                    subscription["browseId"] = navigatePath(itemMap, NAVIGATION_BROWSE_ID, true)
                    subscription["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)

                    val subtitle = navigatePath(
                        itemMap,
                        listOf("subtitle", "runs", 0, "text"),
                        true
                    ) as? String
                    subscription["subscribers"] = subtitle?.split(" ")?.get(0)

                    subscription
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun parseLikedSongs(data: JsonDict): JsonDict {
        val likedSongs = mutableMapOf<String, Any?>()

        val contents = navigatePath(
            data,
            listOf(
                "contents",
                "singleColumnBrowseResultsRenderer",
                "tabs",
                0,
                "tabRenderer",
                "content",
                "sectionListRenderer",
                "contents",
                0,
                "musicPlaylistShelfRenderer",
                "contents"
            ),
            true
        ) as? JsonList
        if (contents != null) {
            likedSongs["tracks"] = contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    SongsParser.parseSong(itemMap)
                } catch (e: Exception) {
                    null
                }
            }
        }

        return likedSongs
    }
}
