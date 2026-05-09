package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.BADGE_LABEL
import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Playlists parser implementation
 */

object PlaylistsParser {
    fun parsePlaylistItem(data: JsonDict): JsonDict {
        val playlistItem = mutableMapOf<String, Any?>()

        playlistItem["id"] = navigatePath(data, listOf("playlistItemData", "videoId"), true)
        playlistItem["videoId"] = playlistItem["id"]
        playlistItem["setVideoId"] = navigatePath(data, listOf("setVideoId"), true)

        val title = navigatePath(data, TITLE_TEXT, true) as? String
        playlistItem["title"] = title

        val artists = SongsParser.parseSongArtists(data, 1)
        playlistItem["artists"] = artists

        val album = SongsParser.parseSongAlbum(data, 2)
        playlistItem["album"] = album

        val duration = getItemText(data, 3)
        playlistItem["duration"] = duration
        playlistItem["duration_seconds"] = parseDuration(duration)

        val thumbnails = navigatePath(data, THUMBNAILS, true)
        playlistItem["thumbnails"] = thumbnails

        val isAvailable = navigatePath(
            data,
            listOf(
                "musicItemRenderer",
                "overlay",
                "musicItemThumbnailOverlayRenderer",
                "content",
                "musicPlayButtonRenderer",
                "playNavigationEndpoint"
            ),
            true
        ) != null
        playlistItem["isAvailable"] = isAvailable

        val isExplicit = navigatePath(data, BADGE_LABEL, true) != null
        playlistItem["isExplicit"] = isExplicit

        val videoType = navigatePath(
            data,
            listOf(
                "musicItemRenderer",
                "overlay",
                "musicItemThumbnailOverlayRenderer",
                "content",
                "musicPlayButtonRenderer",
                "playNavigationEndpoint",
                "watchEndpoint",
                "watchEndpointMusicSupportedConfigs",
                "watchEndpointMusicConfig",
                "musicVideoType"
            ),
            true
        ) as? String
        playlistItem["videoType"] = videoType

        val feedbackTokens = navigatePath(
            data,
            listOf("musicItemRenderer", "menu", "menuRenderer", "items"),
            true
        ) as? JsonList
        if (feedbackTokens != null) {
            val likeToken = feedbackTokens.find { item ->
                navigatePath(
                    item,
                    listOf("toggleMenuServiceItemRenderer", "defaultIcon", "iconType"),
                    true
                ) == "LIKE"
            } as? JsonDict
            if (likeToken != null) {
                playlistItem["likeStatus"] = navigatePath(
                    likeToken,
                    listOf("toggleMenuServiceItemRenderer", "defaultText", "runs", 0, "text"),
                    true
                )
            }
        }

        return playlistItem
    }

    fun parsePlaylist(data: JsonDict): JsonDict {
        val playlist = mutableMapOf<String, Any?>()

        val header =
            navigatePath(data, listOf("header", "musicDetailHeaderRenderer"), true) as? JsonDict
        if (header != null) {
            playlist["title"] = navigatePath(header, TITLE_TEXT, true)
            playlist["thumbnails"] = navigatePath(header, THUMBNAILS, true)
            playlist["description"] =
                navigatePath(header, listOf("description", "runs", 0, "text"), true)

            val secondSubtitleRun =
                navigatePath(header, listOf("subtitle", "runs", 1), true) as? JsonDict
            if (secondSubtitleRun != null) {
                playlist["author"] = parseIdName(secondSubtitleRun)
                playlist["count"] =
                    navigatePath(header, listOf("subtitle", "runs", 2, "text"), true)
            }

            val playlistId = navigatePath(
                header,
                listOf(
                    "menu",
                    "menuRenderer",
                    "topLevelButtons",
                    0,
                    "buttonRenderer",
                    "navigationEndpoint",
                    "watchPlaylistEndpoint",
                    "playlistId"
                ),
                true
            ) as? String
            playlist["playlistId"] = playlistId

            val privacy =
                navigatePath(header, listOf("subtitle", "runs", 4, "text"), true) as? String
            playlist["privacy"] = privacy

            val year = navigatePath(header, listOf("subtitle", "runs", 6, "text"), true) as? String
            playlist["year"] = year
        }

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
            playlist["tracks"] = contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    parsePlaylistItem(itemMap)
                } catch (e: Exception) {
                    null
                }
            }
        }

        return playlist
    }

    fun parsePlaylistItems(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                parsePlaylistItem(item)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parsePlaylistCollaborative(data: JsonDict): JsonDict {
        return parsePlaylist(data) + mapOf("isCollaborative" to true)
    }

    fun parsePlaylistAudio(data: JsonDict): JsonDict {
        return parsePlaylist(data) + mapOf("isAudio" to true)
    }

    fun parsePlaylistChart(data: JsonDict): JsonDict {
        return parsePlaylist(data) + mapOf("isChart" to true)
    }
}
