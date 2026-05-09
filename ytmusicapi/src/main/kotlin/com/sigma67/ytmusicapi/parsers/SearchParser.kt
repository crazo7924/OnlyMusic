package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.*

/**
 * Search parser implementation
 */

val ALL_RESULT_TYPES = listOf(
    "album", "artist", "playlist", "song", "video", "station", "profile", "podcast", "episode"
)

val API_RESULT_TYPES = listOf("single", "ep") + ALL_RESULT_TYPES

object SearchParser {
    fun getSearchResultType(resultTypeLocal: String?, resultTypesLocal: List<String>): String? {
        if (resultTypeLocal.isNullOrBlank()) return null

        val resultType = resultTypeLocal.lowercase()
        return if (resultType in resultTypesLocal) {
            ALL_RESULT_TYPES[resultTypesLocal.indexOf(resultType)]
        } else {
            "album" // default to album
        }
    }

    fun parseTopResult(data: JsonDict, searchResultTypes: List<String>): JsonDict {
        val resultType =
            getSearchResultType(navigatePath(data, SUBTITLE) as? String, searchResultTypes)
        val category = navigatePath(data, CARD_SHELF_TITLE, true) as? String ?: "Top result"

        val searchResult = mutableMapOf<String, Any?>(
            "category" to category,
            "resultType" to resultType
        )

        when (resultType) {
            "artist" -> {
                val subscribers = navigatePath(data, SUBTITLE2, true) as? String
                if (subscribers != null) {
                    searchResult["subscribers"] = subscribers.split(" ")[0]
                }
                val artistInfo = parseSongRuns(
                    navigatePath(data, listOf("title", "runs")) as? JsonList ?: emptyList()
                )
                searchResult.putAll(artistInfo)
            }

            "song", "video" -> {
                val onTap = data["onTap"] as? JsonDict
                if (onTap != null) {
                    searchResult["videoId"] = navigatePath(onTap, WATCH_VIDEO_ID)
                    searchResult["videoType"] = navigatePath(onTap, NAVIGATION_VIDEO_TYPE)
                }

                searchResult["title"] = navigatePath(data, TITLE_TEXT)
                val runs =
                    navigatePath(data, listOf("subtitle", "runs")) as? JsonList ?: emptyList()
                val songInfo = parseSongRuns(runs.drop(2))
                searchResult.putAll(songInfo)
            }

            "album" -> {
                searchResult["browseId"] = navigatePath(data, TITLE + NAVIGATION_BROWSE_ID, true)
                val buttonCommand = navigatePath(
                    data,
                    listOf("buttons", 0, "buttonRenderer", "command"),
                    true
                ) as? JsonDict
                searchResult["playlistId"] =
                    AlbumsParser.parseAlbumPlaylistIdIfExists(buttonCommand)

                searchResult["title"] = navigatePath(data, TITLE_TEXT)
                val runs =
                    navigatePath(data, listOf("subtitle", "runs")) as? JsonList ?: emptyList()
                val albumInfo = parseSongRuns(runs.drop(2))
                searchResult.putAll(albumInfo)
            }

            "playlist" -> {
                searchResult["playlistId"] = navigatePath(data, MENU_PLAYLIST_ID)
                searchResult["title"] = navigatePath(data, TITLE_TEXT)
                val runs =
                    navigatePath(data, listOf("subtitle", "runs")) as? JsonList ?: emptyList()
                searchResult["author"] = parseArtistsRuns(runs.drop(2))
            }

            "episode" -> {
                searchResult["title"] = navigatePath(data, TITLE_TEXT)
                searchResult["videoId"] =
                    navigatePath(data, listOf(*THUMBNAIL_OVERLAY_NAVIGATION, *WATCH_VIDEO_ID))
                searchResult["videoType"] = navigatePath(
                    data,
                    listOf(*THUMBNAIL_OVERLAY_NAVIGATION, *NAVIGATION_VIDEO_TYPE)
                )
                val runs =
                    navigatePath(data, listOf("subtitle", "runs")) as? JsonList ?: emptyList()
                if (runs.size > 2) {
                    searchResult["date"] = (runs[0] as? Map<*, *>)?.get("text")
                    searchResult["podcast"] = parseIdName(runs[2] as JsonDict)
                }
            }
        }

        searchResult["thumbnails"] = navigatePath(data, THUMBNAILS, true)
        return searchResult
    }

    fun parseSearchResult(data: JsonDict, resultType: String?, category: String?): JsonDict {
        val defaultOffset = if (resultType == null || resultType == "album") 2 else 0
        val searchResult = mutableMapOf<String, Any?>("category" to category)

        val videoType = navigatePath(
            data,
            listOf(PLAY_BUTTON, "playNavigationEndpoint", NAVIGATION_VIDEO_TYPE),
            true
        ) as? String

        // Determine result type based on browseId if not provided
        val finalResultType = resultType ?: run {
            val browseId = navigatePath(data, NAVIGATION_BROWSE_ID, true) as? String
            when {
                browseId?.startsWith("VM") == true -> "playlist"
                browseId?.startsWith("RD") == true -> "playlist"
                browseId?.startsWith("VL") == true -> "playlist"
                browseId?.startsWith("MPLA") == true -> "artist"
                browseId?.startsWith("MPRE") == true -> "album"
                else -> null
            }
        }

        searchResult["resultType"] = finalResultType

        // Parse based on result type
        when (finalResultType) {
            "song", "video" -> {
                searchResult["videoId"] =
                    navigatePath(data, listOf("playlistItemData", *WATCH_VIDEO_ID), true)
                        ?: navigatePath(data, WATCH_VIDEO_ID, true)
                searchResult["videoType"] = videoType
                searchResult["title"] = getItemText(data, 0)
                searchResult["artists"] = SongsParser.parseSongArtists(data, 1)
                searchResult["album"] = SongsParser.parseSongAlbum(data, 2)
                val durationText = getItemText(data, 3)
                searchResult["duration"] = durationText
                searchResult["duration_seconds"] = parseDuration(durationText)
            }

            "album" -> {
                searchResult["browseId"] = navigatePath(data, NAVIGATION_BROWSE_ID, true)
                searchResult["title"] = getItemText(data, 0)
                searchResult["type"] = getItemText(data, 1)
                searchResult["artist"] = getItemText(data, 2)
                searchResult["year"] = getItemText(data, 3)
                searchResult["playlistId"] = navigatePath(
                    data,
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
            }

            "playlist" -> {
                searchResult["browseId"] = navigatePath(data, NAVIGATION_BROWSE_ID, true)
                searchResult["title"] = getItemText(data, 0)
                searchResult["author"] = getItemText(data, 1)
                searchResult["itemCount"] = getItemText(data, 2)
                searchResult["playlistId"] = navigatePath(
                    data,
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
            }

            "artist" -> {
                searchResult["browseId"] = navigatePath(data, NAVIGATION_BROWSE_ID, true)
                searchResult["artist"] = getItemText(data, 0)
                searchResult["subscribers"] = getItemText(data, 1)
            }
        }

        searchResult["thumbnails"] = navigatePath(data, THUMBNAILS, true)
        return searchResult
    }

    fun parseSearchResults(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                val resultType =
                    navigatePath(itemMap, listOf("title", "runs", 0, "text"), true) as? String
                parseSearchResult(itemMap, resultType, null)
            } catch (e: Exception) {
                null // Skip invalid items
            }
        }
    }
}
