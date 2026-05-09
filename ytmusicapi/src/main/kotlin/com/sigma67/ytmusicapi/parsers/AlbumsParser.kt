package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.BADGE_LABEL
import com.sigma67.ytmusicapi.DESCRIPTION
import com.sigma67.ytmusicapi.DESCRIPTION_SHELF
import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.RESPONSIVE_HEADER
import com.sigma67.ytmusicapi.SECTION_LIST_ITEM
import com.sigma67.ytmusicapi.SUBTITLE
import com.sigma67.ytmusicapi.SUBTITLE_BADGE_LABEL
import com.sigma67.ytmusicapi.TAB_CONTENT
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.TWO_COLUMN_RENDERER
import com.sigma67.ytmusicapi.WATCH_PID
import com.sigma67.ytmusicapi.WATCH_PLAYLIST_ID
import com.sigma67.ytmusicapi.navigatePath

/**
 * Albums parser implementation
 */

object AlbumsParser {
    fun parseAlbum(data: JsonDict): JsonDict {
        val album = mutableMapOf<String, Any?>()

        album["title"] = navigatePath(data, TITLE_TEXT, true)
        album["browseId"] = navigatePath(data, NAVIGATION_BROWSE_ID, true)
        album["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val subtitleRuns = navigatePath(data, listOf("subtitle", "runs"), true) as? JsonList
        if (subtitleRuns != null && subtitleRuns.size >= 2) {
            album["type"] = subtitleRuns[0]["text"]
            album["artist"] = subtitleRuns[2]["text"]
            if (subtitleRuns.size >= 4) {
                album["year"] = subtitleRuns[4]["text"]
            }
        }

        album["isExplicit"] = navigatePath(data, BADGE_LABEL, true) != null

        return album
    }

    fun parseAlbumHeader2024(data: JsonDict): JsonDict {
        val header = navigatePath(
            data,
            listOf(TWO_COLUMN_RENDERER, TAB_CONTENT, SECTION_LIST_ITEM, RESPONSIVE_HEADER),
            true
        ) as? JsonDict ?: return emptyMap()

        val album = mutableMapOf(
            "title" to navigatePath(header, TITLE_TEXT, true),
            "type" to navigatePath(header, SUBTITLE, true),
            "thumbnails" to navigatePath(header, THUMBNAILS, true),
            "isExplicit" to (navigatePath(header, SUBTITLE_BADGE_LABEL, true) != null)
        )

        // Description
        album["description"] =
            navigatePath(header, listOf("description", DESCRIPTION_SHELF, DESCRIPTION), true)

        // Album info from subtitle runs
        val subtitleRuns = navigatePath(header, listOf("subtitle", "runs"), true) as? JsonList
        if (subtitleRuns != null && subtitleRuns.size >= 3) {
            val albumInfo = parseSongRuns(subtitleRuns.drop(2))
            album.putAll(albumInfo)
        }

        // Artists from strapline
        val straplineRuns =
            navigatePath(header, listOf("straplineTextOne", "runs"), true) as? JsonList
        album["artists"] = if (straplineRuns != null) parseArtistsRuns(straplineRuns) else null

        // Track count and duration from second subtitle
        val secondSubtitleRuns =
            navigatePath(header, listOf("secondSubtitle", "runs"), true) as? JsonList
        if (secondSubtitleRuns != null && secondSubtitleRuns.size > 1) {
            album["trackCount"] = secondSubtitleRuns[0]["text"]
            album["duration"] = secondSubtitleRuns[2]["text"]
        } else if (secondSubtitleRuns != null && secondSubtitleRuns.size == 1) {
            album["duration"] = secondSubtitleRuns[0]["text"]
        }

        // Audio playlist ID and like status
        val buttons = navigatePath(header, listOf("buttons"), true) as? JsonList
        if (buttons != null) {
            val playButton = buttons.find { button ->
                navigatePath(button, listOf("musicPlayButtonRenderer"), true) != null
            }

            if (playButton != null) {
                album["audioPlaylistId"] = navigatePath(
                    playButton,
                    listOf("musicPlayButtonRenderer", "playNavigationEndpoint", WATCH_PID),
                    true
                )
                    ?: navigatePath(
                        playButton,
                        listOf(
                            "musicPlayButtonRenderer",
                            "playNavigationEndpoint",
                            WATCH_PLAYLIST_ID
                        ),
                        true
                    )
            }

            val toggleButton = buttons.find { button ->
                navigatePath(button, listOf("toggleButtonRenderer"), true) != null
            }

            if (toggleButton != null) {
                val service = navigatePath(
                    toggleButton,
                    listOf("toggleButtonRenderer", "defaultServiceEndpoint"),
                    true
                ) as? JsonDict
                album["likeStatus"] =
                    if (service != null) parseLikeStatus(service) else "INDIFFERENT"
            } else {
                album["likeStatus"] = "INDIFFERENT"
            }
        }

        return album
    }

    fun parseAlbums(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                parseAlbum(item)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parseAlbumPlaylistIdIfExists(data: JsonDict?): String? {
        if (data == null) return null
        return navigatePath(data, WATCH_PID, true) as? String
            ?: navigatePath(data, WATCH_PLAYLIST_ID, true) as? String
    }
}