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
 * Explore parser implementation
 */

object ExploreParser {
    fun parseExplore(data: JsonDict): JsonDict {
        val explore = mutableMapOf<String, Any?>()

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
                "contents"
            ),
            true
        ) as? JsonList
        if (contents != null) {
            val sections = mutableListOf<JsonDict>()

            for (section in contents) {
                val sectionMap = section as? JsonDict ?: continue
                val parsedSection = parseExploreSection(sectionMap)
                if (parsedSection.isNotEmpty()) {
                    sections.add(parsedSection)
                }
            }

            explore["sections"] = sections
        }

        return explore
    }

    fun parseExploreSection(data: JsonDict): JsonDict {
        val section = mutableMapOf<String, Any?>()

        val shelfRenderer = navigatePath(data, listOf("musicShelfRenderer"), true) as? JsonDict
        if (shelfRenderer != null) {
            section["title"] = navigatePath(shelfRenderer, TITLE_TEXT, true)
            section["contents"] = navigatePath(shelfRenderer, listOf("contents"), true)
        }

        val carouselLockupRenderer =
            navigatePath(data, listOf("musicCarouselShelfRenderer"), true) as? JsonDict
        if (carouselLockupRenderer != null) {
            section["title"] = navigatePath(
                carouselLockupRenderer,
                listOf(
                    "header",
                    "musicCarouselShelfBasicHeaderRenderer",
                    "title",
                    "runs",
                    0,
                    "text"
                ),
                true
            )
            section["contents"] = navigatePath(carouselLockupRenderer, listOf("contents"), true)
        }

        return section
    }

    fun parseMood(data: JsonDict): JsonDict {
        val mood = mutableMapOf<String, Any?>()

        mood["title"] = navigatePath(data, TITLE_TEXT, true)
        mood["params"] =
            navigatePath(data, listOf("navigationEndpoint", "browseEndpoint", "params"), true)
        mood["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        return mood
    }

    fun parseMoods(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                parseMood(itemMap)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parseGenre(data: JsonDict): JsonDict {
        val genre = mutableMapOf<String, Any?>()

        genre["title"] = navigatePath(data, TITLE_TEXT, true)
        genre["params"] =
            navigatePath(data, listOf("navigationEndpoint", "browseEndpoint", "params"), true)
        genre["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        return genre
    }

    fun parseGenres(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                parseGenre(item)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parseCharts(data: JsonDict): JsonDict {
        val charts = mutableMapOf<String, Any?>()

        val contents = navigatePath(
            data,
            listOf("contents", "singleColumnBrowseResultsRenderer", "tabs"),
            true
        ) as? JsonList
        if (contents != null) {
            for (tab in contents) {
                val title =
                    navigatePath(tab, listOf("tabRenderer", "title"), true) as? String ?: continue

                val tabContents = navigatePath(
                    tab,
                    listOf("tabRenderer", "content", "sectionListRenderer", "contents"),
                    true
                ) as? JsonList ?: continue

                when {
                    title.contains("Top songs", ignoreCase = true) -> {
                        charts["songs"] = parseChartSongs(tabContents)
                    }

                    title.contains("Top videos", ignoreCase = true) -> {
                        charts["videos"] = parseChartVideos(tabContents)
                    }

                    title.contains("Top artists", ignoreCase = true) -> {
                        charts["artists"] = parseChartArtists(tabContents)
                    }
                }
            }
        }

        return charts
    }

    fun parseChartSongs(data: JsonList): JsonList {
        return data.flatMap { section ->
            val shelfRenderer =
                navigatePath(section, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val song = SongsParser.parseSong(item).toMutableMap()
                    song["rank"] = navigatePath(
                        item,
                        listOf("musicItemRenderer", "index", "runs", 0, "text"),
                        true
                    )
                    song
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    fun parseChartVideos(data: JsonList): JsonList {
        return data.flatMap { section ->
            val shelfRenderer =
                navigatePath(section, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    val video = mutableMapOf<String, Any?>()
                    video["title"] = navigatePath(itemMap, TITLE_TEXT, true)
                    video["videoId"] = navigatePath(itemMap, WATCH_VIDEO_ID, true)
                    video["videoType"] = navigatePath(itemMap, NAVIGATION_VIDEO_TYPE, true)
                    video["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)
                    video["rank"] = navigatePath(
                        itemMap,
                        listOf("musicItemRenderer", "index", "runs", 0, "text"),
                        true
                    )

                    val runs = navigatePath(itemMap, listOf("subtitle", "runs"), true) as? JsonList
                    if (runs != null && runs.size >= 2) {
                        video["artists"] = listOf(parseIdName(runs[0]))
                        video["views"] = runs[1]["text"]
                    }

                    video
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    fun parseChartArtists(data: JsonList): JsonList {
        return data.flatMap { section ->
            val shelfRenderer =
                navigatePath(section, listOf("musicShelfRenderer"), true) as? JsonDict
                    ?: return@flatMap emptyList()
            val contents = navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                ?: return@flatMap emptyList()

            contents.mapNotNull { item ->
                try {
                    val artist = mutableMapOf<String, Any?>()
                    artist["title"] = navigatePath(item, TITLE_TEXT, true)
                    artist["browseId"] = navigatePath(item, NAVIGATION_BROWSE_ID, true)
                    artist["thumbnails"] = navigatePath(item, THUMBNAILS, true)
                    artist["rank"] = navigatePath(
                        item,
                        listOf("musicItemRenderer", "index", "runs", 0, "text"),
                        true
                    )

                    val subtitle =
                        navigatePath(item, listOf("subtitle", "runs", 0, "text"), true) as? String
                    artist["subscribers"] = subtitle?.split(" ")?.get(0)

                    artist
                } catch (_: Exception) {
                    null
                }
            }
        }
    }
}
