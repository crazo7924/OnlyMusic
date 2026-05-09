package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Watch parser implementation
 */

object WatchParser {
    fun parseWatchPlaylist(data: JsonDict): JsonDict {
        val watchPlaylist = mutableMapOf<String, Any?>()

        val playlist = navigatePath(
            data,
            listOf(
                "contents",
                "singleColumnMusicWatchNextResultsRenderer",
                "tabbedRenderer",
                "watchNextTabbedResultsRenderer",
                "tabs",
                0,
                "tabRenderer",
                "content",
                "musicQueueRenderer",
                "content",
                "playlistPanelRenderer"
            ),
            true
        ) as? JsonDict
        if (playlist != null) {
            val tracks = navigatePath(playlist, listOf("contents"), true) as? JsonList
            if (tracks != null) {
                watchPlaylist["tracks"] = tracks.mapNotNull { item ->
                    try {
                        parseWatchTrack(item)
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            watchPlaylist["lyrics"] = navigatePath(playlist, listOf("lyricsPanelRenderer"), true)
        }

        val currentVideoEndpoint =
            navigatePath(data, listOf("currentVideoEndpoint", "watchEndpoint"), true) as? JsonDict
        if (currentVideoEndpoint != null) {
            watchPlaylist["videoId"] = navigatePath(currentVideoEndpoint, listOf("videoId"), true)
            watchPlaylist["playlistId"] =
                navigatePath(currentVideoEndpoint, listOf("playlistId"), true)
        }

        return watchPlaylist
    }

    fun parseWatchTrack(data: JsonDict): JsonDict {
        val track = mutableMapOf<String, Any?>()

        val trackInfo = navigatePath(data, listOf("playlistPanelVideoRenderer"), true) as? JsonDict
        if (trackInfo != null) {
            track["videoId"] = navigatePath(trackInfo, listOf("videoId"), true)
            track["title"] = navigatePath(trackInfo, TITLE_TEXT, true)
            track["length"] = navigatePath(trackInfo, listOf("lengthText", "runs", 0, "text"), true)
            track["thumbnails"] = navigatePath(trackInfo, THUMBNAILS, true)
            track["isCurrent"] = navigatePath(trackInfo, listOf("selected"), true) ?: false

            val artists =
                navigatePath(trackInfo, listOf("longBylineText", "runs"), true) as? JsonList
            if (artists != null) {
                track["artists"] = parseArtistsRuns(artists)
            }

            val album = navigatePath(trackInfo, listOf("longBylineText", "runs"), true) as? JsonList
            if (album != null && album.size > 2) {
                track["album"] = parseIdName(album[2])
            }
        }

        return track
    }

    fun parseWatchNext(data: JsonDict): JsonDict {
        val watchNext = mutableMapOf<String, Any?>()

        val tabs = navigatePath(
            data,
            listOf(
                "contents",
                "singleColumnMusicWatchNextResultsRenderer",
                "tabbedRenderer",
                "watchNextTabbedResultsRenderer",
                "tabs"
            ),
            true
        ) as? JsonList
        if (tabs != null) {
            val relatedTab = tabs.find { tab ->
                val tabMap = tab as? JsonDict
                navigatePath(tabMap, listOf("tabRenderer", "title"), true) == "Up next"
            } as? JsonDict

            if (relatedTab != null) {
                val contents = navigatePath(
                    relatedTab,
                    listOf(
                        "tabRenderer",
                        "content",
                        "musicQueueRenderer",
                        "content",
                        "playlistPanelRenderer",
                        "contents"
                    ),
                    true
                ) as? JsonList
                if (contents != null) {
                    watchNext["related"] = contents.mapNotNull { item ->
                        try {
                            parseWatchTrack(item)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }
        }

        return watchNext
    }

    fun parseLyrics(data: JsonDict): JsonDict {
        val lyrics = mutableMapOf<String, Any?>()

        lyrics["lyrics"] = navigatePath(data, listOf("lyricsPanelRenderer", "lyrics", "runs"), true)
        lyrics["source"] =
            navigatePath(data, listOf("lyricsPanelRenderer", "source", "runs", 0, "text"), true)

        return lyrics
    }
}
