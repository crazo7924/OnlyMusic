package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.BADGE_LABEL
import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Uploads parser implementation
 */

object UploadsParser {
    fun parseUpload(data: JsonDict): JsonDict {
        val upload = mutableMapOf<String, Any?>()

        upload["entityId"] = navigatePath(data, listOf("entityId"), true)
        upload["videoId"] = navigatePath(data, listOf("videoId"), true)
        upload["title"] = navigatePath(data, TITLE_TEXT, true)
        upload["artist"] = navigatePath(data, listOf("subtitle", "runs", 0, "text"), true)
        upload["album"] = navigatePath(data, listOf("subtitle", "runs", 2, "text"), true)

        val duration = navigatePath(data, listOf("subtitle", "runs", 4, "text"), true) as? String
        upload["duration"] = duration
        upload["duration_seconds"] = parseDuration(duration ?: "")

        upload["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        val likeStatus =
            navigatePath(data, listOf("menu", "menuRenderer", "items"), true) as? JsonList
        if (likeStatus != null) {
            val likeItem = likeStatus.find { item ->
                val itemMap = item as? JsonDict
                navigatePath(
                    itemMap,
                    listOf("toggleMenuServiceItemRenderer", "defaultIcon", "iconType"),
                    true
                ) == "LIKE"
            } as? JsonDict
            if (likeItem != null) {
                upload["likeStatus"] = navigatePath(
                    likeItem,
                    listOf("toggleMenuServiceItemRenderer", "defaultText", "runs", 0, "text"),
                    true
                )
            }
        }

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
        upload["isAvailable"] = isAvailable

        val isExplicit = navigatePath(data, BADGE_LABEL, true) != null
        upload["isExplicit"] = isExplicit

        return upload
    }

    fun parseUploads(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                parseUpload(itemMap)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parseUploadEntity(data: JsonDict): JsonDict {
        val entity = mutableMapOf<String, Any?>()

        entity["entityId"] = navigatePath(data, listOf("entityId"), true)
        entity["videoId"] = navigatePath(data, listOf("videoId"), true)
        entity["title"] = navigatePath(data, TITLE_TEXT, true)
        entity["artist"] = navigatePath(data, listOf("subtitle", "runs", 0, "text"), true)
        entity["album"] = navigatePath(data, listOf("subtitle", "runs", 2, "text"), true)
        entity["duration"] = navigatePath(data, listOf("subtitle", "runs", 4, "text"), true)
        entity["thumbnails"] = navigatePath(data, THUMBNAILS, true)

        return entity
    }

    fun parseUploadEntities(data: JsonList): JsonList {
        return data.mapNotNull { item ->
            try {
                parseUploadEntity(item)
            } catch (_: Exception) {
                null
            }
        }
    }
}
