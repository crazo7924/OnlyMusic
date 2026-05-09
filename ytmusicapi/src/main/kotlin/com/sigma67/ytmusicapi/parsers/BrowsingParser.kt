package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.BADGE_LABEL
import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.NAVIGATION_VIDEO_TYPE
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.WATCH_VIDEO_ID
import com.sigma67.ytmusicapi.navigatePath

/**
 * Browsing parser implementation
 */

object BrowsingParser {
    fun parseArtist(data: JsonDict): JsonDict {
        val artist = mutableMapOf<String, Any?>()

        val header =
            navigatePath(data, listOf("header", "musicImmersiveHeaderRenderer"), true) as? JsonDict
        if (header != null) {
            artist["name"] = navigatePath(header, TITLE_TEXT, true)
            artist["description"] =
                navigatePath(header, listOf("description", "runs", 0, "text"), true)
            artist["views"] = navigatePath(
                header,
                listOf(
                    "subscriptionButton",
                    "subscribeButtonRenderer",
                    "subscriberCountText",
                    "runs",
                    0,
                    "text"
                ),
                true
            )
            artist["thumbnails"] = navigatePath(header, THUMBNAILS, true)
            artist["musicVideos"] = emptyList<JsonDict>()
            artist["albums"] = emptyList<JsonDict>()
            artist["singles"] = emptyList<JsonDict>()
            artist["playlists"] = emptyList<JsonDict>()
        }

        val contents = navigatePath(
            data,
            listOf("contents", "singleColumnBrowseResultsRenderer", "tabs"),
            true
        ) as? JsonList
        if (contents != null) {
            for (tab in contents) {
                val tabContent = navigatePath(
                    tab,
                    listOf("tabRenderer", "content", "sectionListRenderer", "contents"),
                    true
                ) as? JsonList ?: continue

                for (section in tabContent) {
                    val shelfRenderer =
                        navigatePath(section, listOf("musicShelfRenderer"), true) as? JsonDict
                            ?: continue

                    val title = navigatePath(shelfRenderer, TITLE_TEXT, true) as? String ?: continue
                    val contents =
                        navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList
                            ?: continue

                    when {
                        title.contains("Albums", ignoreCase = true) -> {
                            artist["albums"] = AlbumsParser.parseAlbums(contents)
                        }

                        title.contains("Singles", ignoreCase = true) -> {
                            artist["singles"] = AlbumsParser.parseAlbums(contents)
                        }

                        title.contains("Videos", ignoreCase = true) -> {
                            artist["musicVideos"] = contents.mapNotNull { item ->
                                try {
                                    val itemMap = item as JsonDict
                                    parseMusicVideo(itemMap)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }

                        title.contains("Playlists", ignoreCase = true) -> {
                            artist["playlists"] = contents.mapNotNull { item ->
                                try {
                                    val itemMap = item as JsonDict
                                    parsePlaylistCard(itemMap)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                    }
                }
            }
        }

        return artist
    }

    fun parseAlbum(data: JsonDict): JsonDict {
        val album = mutableMapOf<String, Any?>()

        val header =
            navigatePath(data, listOf("header", "musicDetailHeaderRenderer"), true) as? JsonDict
        if (header != null) {
            album["title"] = navigatePath(header, TITLE_TEXT, true)
            album["type"] = navigatePath(header, listOf("subtitle", "runs", 0, "text"), true)
            album["thumbnails"] = navigatePath(header, THUMBNAILS, true)
            album["isExplicit"] = navigatePath(header, BADGE_LABEL, true) != null

            val year = navigatePath(header, listOf("subtitle", "runs", 2, "text"), true) as? String
            album["year"] = year?.toIntOrNull()

            album["artists"] = listOf(
                parseIdName(
                    navigatePath(
                        header,
                        listOf("subtitle", "runs", 4),
                        true
                    ) as JsonDict
                )
            )

            album["playlistId"] = navigatePath(
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
            )
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
            album["tracks"] = contents.mapNotNull { item ->
                try {
                    val itemMap = item as JsonDict
                    SongsParser.parseSong(itemMap)
                } catch (e: Exception) {
                    null
                }
            }
        }

        return album
    }

    fun parseMusicVideo(data: JsonDict): JsonDict {
        val video = mutableMapOf<String, Any?>()

        video["title"] = navigatePath(data, TITLE_TEXT, true)
        video["videoId"] = navigatePath(data, WATCH_VIDEO_ID, true)
        video["videoType"] = navigatePath(data, NAVIGATION_VIDEO_TYPE, true)
        video["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val runs = navigatePath(data, listOf("subtitle", "runs"), true) as? JsonList
        if (runs != null && runs.size >= 2) {
            video["artists"] = listOf(parseIdName(runs[0]))
            video["views"] = runs[1]["text"]
        }

        return video
    }

    fun parsePlaylistCard(data: JsonDict): JsonDict {
        val playlist = mutableMapOf<String, Any?>()

        playlist["title"] = navigatePath(data, TITLE_TEXT, true)
        playlist["playlistId"] = navigatePath(
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
        playlist["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val subtitleRuns = navigatePath(data, listOf("subtitle", "runs"), true) as? JsonList
        if (!subtitleRuns.isNullOrEmpty()) {
            subtitleRuns[0]["text"].also { playlist["author"] = it }
        }

        return playlist
    }

    fun parseArtistContent(data: JsonDict): JsonDict {
        val content = mutableMapOf<String, Any?>()

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
            for (section in contents) {
                val sectionMap = section as? JsonDict ?: continue
                val shelfRenderer =
                    navigatePath(sectionMap, listOf("musicShelfRenderer"), true) as? JsonDict
                        ?: continue

                val title = navigatePath(shelfRenderer, TITLE_TEXT, true) as? String ?: continue
                val items =
                    navigatePath(shelfRenderer, listOf("contents"), true) as? JsonList ?: continue

                when {
                    title.contains("Songs", ignoreCase = true) -> {
                        content["songs"] = items.mapNotNull { item ->
                            try {
                                val itemMap = item as JsonDict
                                SongsParser.parseSong(itemMap)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    title.contains("Albums", ignoreCase = true) -> {
                        content["albums"] = AlbumsParser.parseAlbums(items)
                    }

                    title.contains("Singles", ignoreCase = true) -> {
                        content["singles"] = AlbumsParser.parseAlbums(items)
                    }

                    title.contains("Videos", ignoreCase = true) -> {
                        content["videos"] = items.mapNotNull { item ->
                            try {
                                parseMusicVideo(item)
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }
                }
            }
        }

        return content
    }
}
