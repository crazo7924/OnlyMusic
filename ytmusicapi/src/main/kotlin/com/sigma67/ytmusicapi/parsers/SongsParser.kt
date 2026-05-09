package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.MENU_ITEMS
import com.sigma67.ytmusicapi.MENU_LIKE_STATUS
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.NAVIGATION_VIDEO_TYPE
import com.sigma67.ytmusicapi.TEXT_RUN
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TOGGLE_MENU
import com.sigma67.ytmusicapi.WATCH_VIDEO_ID
import com.sigma67.ytmusicapi.navigatePath

/**
 * Songs parser implementation
 */

object SongsParser {
    fun parseSongArtists(data: JsonDict, index: Int): JsonList {
        val flexItem = getFlexColumnItem(data, index) ?: return emptyList()
        val runs = (flexItem["text"] as? JsonDict) ?: return emptyList()
        val runsList = (runs["runs"] as? JsonList) ?: return emptyList()

        return parseArtistsRuns(runsList)
    }

    fun parseSongAlbum(data: JsonDict, index: Int): JsonDict? {
        val flexItem = getFlexColumnItem(data, index) ?: return null
        val browseId = navigatePath(flexItem, TEXT_RUN + NAVIGATION_BROWSE_ID, true) as? String
        return mapOf(
            "name" to getItemText(data, index),
            "id" to browseId
        )
    }

    fun parseSongMenuData(data: JsonDict): JsonDict {
        val menuData = mutableMapOf<String, Any?>()

        // Parse inLibrary status
        val menuItems = navigatePath(data, MENU_ITEMS, true) as? JsonList
        val toggleMenuItems = menuItems?.filter { it.containsKey(TOGGLE_MENU) }

        if (toggleMenuItems != null) {
            for (item in toggleMenuItems) {
                val toggleItem = item[TOGGLE_MENU] as? JsonDict
                val defaultText = navigatePath(
                    toggleItem,
                    listOf("defaultText", "runs", 0, "text"), true
                ) as? String

                when (defaultText) {
                    "Remove from library" -> menuData["inLibrary"] = true
                    "Add to library" -> menuData["inLibrary"] = false
                }
            }
        }

        // Parse feedback tokens
        val feedbackTokens = mutableMapOf<String, Any?>()
        if (menuItems != null) {
            for (item in menuItems) {
                val service = item["menuServiceItemRenderer"] as? JsonDict
                val text = navigatePath(
                    service,
                    listOf("text", "runs", 0, "text"), true
                ) as? String

                when (text) {
                    "Not interested" -> {
                        val endpoint = service?.get("serviceEndpoint") as? JsonDict
                        feedbackTokens["add"] = navigatePath(
                            endpoint ?: emptyMap(),
                            listOf("feedbackEndpoint", "feedbackToken"), true
                        )
                    }

                    "Undo not interested" -> {
                        val endpoint = service?.get("serviceEndpoint") as? JsonDict
                        feedbackTokens["remove"] = navigatePath(
                            endpoint ?: emptyMap(),
                            listOf("feedbackEndpoint", "feedbackToken"), true
                        )
                    }
                }
            }
        }

        menuData["feedbackTokens"] = feedbackTokens

        // Parse like status
        val likeStatus = parseLikeStatus(data)
        menuData["likeStatus"] = likeStatus

        return menuData
    }

    fun parseLikeStatus(data: JsonDict): String {
        val toggledButton = navigatePath(data, MENU_LIKE_STATUS, true) as? String
        return when (toggledButton) {
            "LIKE" -> "LIKE"
            "DISLIKE" -> "DISLIKE"
            else -> "INDIFFERENT"
        }
    }

    fun parseSong(data: JsonDict): JsonDict {
        val song = mutableMapOf<String, Any?>()

        // Basic info
        song["videoId"] = navigatePath(data, listOf("playlistItemData", WATCH_VIDEO_ID), true)
            ?: navigatePath(data, WATCH_VIDEO_ID.toList(), true)

        song["title"] = getItemText(data, 0)

        // Artists
        song["artists"] = parseSongArtists(data, 1)

        // Album
        song["album"] = parseSongAlbum(data, 2)

        // Duration
        val durationText = getItemText(data, 3)
        song["duration"] = durationText
        song["duration_seconds"] = parseDuration(durationText)

        // Menu data
        song.putAll(parseSongMenuData(data))

        // Thumbnails
        song["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        // Video type
        song["videoType"] = navigatePath(data, NAVIGATION_VIDEO_TYPE, true)

        // Set video ID (for playlists)
        song["setVideoId"] = navigatePath(data, listOf("setVideoId"), true)

        return song
    }

    fun parseSongs(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                parseSong(item)
            } catch (_: Exception) {
                null // Skip invalid items
            }
        }
    }
}
