package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.NAVIGATION_VIDEO_TYPE
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.WATCH_VIDEO_ID
import com.sigma67.ytmusicapi.navigatePath

/**
 * Podcasts parser implementation
 */

object PodcastsParser {
    fun parsePodcast(data: JsonDict): JsonDict {
        val podcast = mutableMapOf<String, Any?>()

        podcast["title"] = navigatePath(data, TITLE_TEXT, true)
        podcast["browseId"] = navigatePath(data, NAVIGATION_BROWSE_ID, true)
        podcast["author"] = navigatePath(data, listOf("subtitle", "runs", 0, "text"), true)
        podcast["description"] = navigatePath(data, listOf("description", "runs", 0, "text"), true)
        podcast["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val totalEpisodes =
            navigatePath(data, listOf("subtitle", "runs", 2, "text"), true) as? String
        podcast["totalEpisodes"] = totalEpisodes?.split(" ")?.get(0)?.toIntOrNull()

        return podcast
    }

    fun parseEpisode(data: JsonDict): JsonDict {
        val episode = mutableMapOf<String, Any?>()

        episode["title"] = navigatePath(data, TITLE_TEXT, true)
        episode["description"] = navigatePath(data, listOf("description", "runs", 0, "text"), true)
        episode["videoId"] = navigatePath(data, WATCH_VIDEO_ID, true)
        episode["videoType"] = navigatePath(data, NAVIGATION_VIDEO_TYPE, true)
        episode["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val subtitleRuns = navigatePath(data, listOf("subtitle", "runs"), true) as? JsonList
        if (subtitleRuns != null && subtitleRuns.size >= 3) {
            episode["date"] = (subtitleRuns[0] as? JsonDict)?.get("text")
            episode["duration"] = (subtitleRuns[2] as? JsonDict)?.get("text")
            episode["duration_seconds"] = parseDuration(episode["duration"] as? String ?: "")
        }

        return episode
    }

    fun parsePodcastEpisodes(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                parseEpisode(itemMap)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parsePodcasts(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                parsePodcast(itemMap)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parsePodcastBrowse(data: JsonDict): JsonDict {
        val podcastBrowse = mutableMapOf<String, Any?>()

        val header =
            navigatePath(data, listOf("header", "musicDetailHeaderRenderer"), true) as? JsonDict
        if (header != null) {
            podcastBrowse["title"] = navigatePath(header, TITLE_TEXT, true)
            podcastBrowse["author"] =
                navigatePath(header, listOf("subtitle", "runs", 0, "text"), true)
            podcastBrowse["description"] =
                navigatePath(header, listOf("description", "runs", 0, "text"), true)
            podcastBrowse["thumbnails"] = navigatePath(header, THUMBNAILS, true)
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
                "musicShelfRenderer",
                "contents"
            ),
            true
        ) as? JsonList
        if (contents != null) {
            podcastBrowse["episodes"] = parsePodcastEpisodes(contents)
        }

        return podcastBrowse
    }
}
