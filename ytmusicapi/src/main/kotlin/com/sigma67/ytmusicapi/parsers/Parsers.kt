package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Parser functions for various API response types
 */

internal object Parsers {
    fun parseAlbum(results: JsonDict): JsonDict {
        return AlbumsParser.parseAlbum(results)
    }

    fun parseAlbumHeader2024(results: JsonDict): JsonDict {
        return AlbumsParser.parseAlbumHeader2024(results)
    }

    fun parseArtist(results: JsonDict): JsonDict {
        return BrowsingParser.parseArtist(results)
    }

    fun parsePlaylist(results: JsonDict): JsonDict {
        return PlaylistsParser.parsePlaylist(results)
    }

    fun parsePlaylistItems(results: JsonList): JsonList {
        return PlaylistsParser.parsePlaylistItems(results)
    }

    fun parseSong(results: JsonDict): JsonDict {
        return SongsParser.parseSong(results)
    }

    fun parseContentList(results: JsonList, parseFunc: (JsonDict) -> JsonDict): JsonList {
        return results.mapNotNull { item ->
            try {
                parseFunc(item)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parseMixedContent(results: JsonList): JsonList {
        return results.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                val contentType = navigatePath(
                    itemMap,
                    listOf("musicTwoRowItemRenderer"),
                    true
                )?.let { "twoRowItem" }
                    ?: navigatePath(
                        itemMap,
                        listOf("musicResponsiveListItemRenderer"),
                        true
                    )?.let { "responsiveListItem" }
                    ?: "unknown"

                when (contentType) {
                    "twoRowItem" -> parseTwoRowItem(itemMap)
                    "responsiveListItem" -> parseResponsiveListItem(itemMap)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseTwoRowItem(data: JsonDict): JsonDict {
        val item = mutableMapOf<String, Any?>()
        item["title"] = navigatePath(data, TITLE_TEXT, true)
        item["thumbnails"] = navigatePath(data, THUMBNAILS, true)
        item["subtitle"] = navigatePath(data, listOf("subtitle", "runs", 0, "text"), true)
        return item
    }

    private fun parseResponsiveListItem(data: JsonDict): JsonDict {
        val item = mutableMapOf<String, Any?>()
        item["title"] = navigatePath(data, TITLE_TEXT, true)
        item["thumbnails"] = navigatePath(data, THUMBNAILS, true)
        item["subtitle"] = navigatePath(data, listOf("subtitle", "runs", 0, "text"), true)
        return item
    }

    fun parseAlbums(results: JsonList): JsonList {
        return AlbumsParser.parseAlbums(results)
    }

    fun parseArtists(results: JsonList): JsonList {
        return ArtistsParser.parseArtists(results)
    }

    fun parseSongs(results: JsonList): JsonList {
        return results.mapNotNull { item ->
            try {
                SongsParser.parseSong(item)
            } catch (_: Exception) {
                null
            }
        }
    }
}
