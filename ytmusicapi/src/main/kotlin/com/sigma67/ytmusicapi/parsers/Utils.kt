package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.ICON_TYPE
import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.MENU_ITEMS
import com.sigma67.ytmusicapi.MNIR
import com.sigma67.ytmusicapi.MutableJsonDict
import com.sigma67.ytmusicapi.MutableJsonList
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.navigatePath
import java.util.regex.Pattern

/**
 * Parser utility functions
 */

val DOT_SEPARATOR_RUN = mapOf("text" to " • ")

fun parseMenuPlaylists(data: JsonDict, result: MutableMap<String, Any?>) {
    val menuItems = navigatePath(data, MENU_ITEMS, true) as? JsonList ?: return

    val watchMenu = menuItems.filter { item ->
        item.containsKey(MNIR)
    }

    for (item in watchMenu) {
        val itemMap = item as Map<*, *>
        val mnirData = itemMap[MNIR] as? JsonDict
        val icon = navigatePath(mnirData ?: emptyMap(), ICON_TYPE, true) as? String

        val watchKey = when (icon) {
            "MUSIC_SHUFFLE" -> "shuffleId"
            "MIX" -> "radioId"
            else -> continue
        }

        val watchId = navigatePath(
            mnirData ?: emptyMap(),
            listOf("navigationEndpoint", "watchPlaylistEndpoint", "playlistId"), true
        ) as? String
            ?: navigatePath(
                mnirData ?: emptyMap(),
                listOf("navigationEndpoint", "watchEndpoint", "playlistId"), true
            ) as? String

        if (watchId != null) {
            result[watchKey] = watchId
        }
    }
}

fun getItemText(
    item: JsonDict,
    index: Int,
    runIndex: Int = 0,
    noneIfAbsent: Boolean = false,
): String? {
    val column = getFlexColumnItem(item, index) ?: return null

    val runs = column["text"] as? Map<*, *>? ?: return null
    val runsList = runs["runs"] as? List<*> ?: return null

    if (noneIfAbsent && runsList.size < runIndex + 1) return null

    val run = runsList[runIndex] as? Map<*, *>
    return run?.get("text") as? String
}

fun getFlexColumnItem(item: JsonDict, index: Int): JsonDict? {
    val flexColumns = item["flexColumns"] as? JsonList ?: return null
    if (flexColumns.size <= index) return null

    val column = flexColumns[index]
    val renderer = column["musicResponsiveListItemFlexColumnRenderer"] as? JsonDict
    val text = renderer?.get("text") as? JsonDict
    if (text?.get("runs") !is List<*>) {
        return null
    }

    return renderer
}

fun getFixedColumnItem(item: JsonDict, index: Int): JsonDict? {
    val fixedColumns = item["fixedColumns"] as? JsonList ?: return null
    if (fixedColumns.size <= index) return null

    val column = fixedColumns[index]
    val renderer = column["musicResponsiveListItemFixedColumnRenderer"] as JsonDict
    val text = renderer["text"] as? JsonDict
    if (text?.get("runs") !is List<*>) return null

    return renderer
}

fun getDotSeparatorIndex(runs: JsonList): Int {
    return runs.indexOf(DOT_SEPARATOR_RUN).takeIf { it >= 0 } ?: runs.size
}

fun parseDuration(duration: String?): Int? {
    if (duration.isNullOrBlank()) return null

    val durationSplit = duration.trim().split(":")
    if (durationSplit.any { !it.matches(Regex("\\d+")) }) return null

    val multipliers = listOf(1, 60, 3600)
    return durationSplit.reversed().zip(multipliers).sumOf { (time, multiplier) ->
        multiplier * time.toInt()
    }
}

fun parseLikeStatus(service: JsonDict): String {
    val status = navigatePath(service, listOf("likeEndpoint", "status"), true) as? String
    return status ?: "INDIFFERENT"
}

fun parseIdName(data: JsonDict): Map<String, String?> {
    return mapOf(
        "name" to (data["text"] as? String),
        "id" to (navigatePath(data, NAVIGATION_BROWSE_ID, true) as? String)
    )
}

fun findObjectByKey(items: List<Any?>, key: String): Map<String, Any?>? {
    return items.filterIsInstance<Map<String, Any?>>().find { it.containsKey(key) }
}

fun parseArtistsRuns(runs: JsonList): JsonList {
    return runs.mapNotNull { run ->
        val text = run["text"] as? String
        val browseId = navigatePath(run, NAVIGATION_BROWSE_ID, true) as? String

        if (text != null) {
            mapOf("name" to text, "id" to browseId)
        } else null
    }
}

fun parseSongRuns(runs: JsonList, skipTypeSpec: Boolean = false): JsonDict {
    val parsed: MutableJsonDict = mutableMapOf()
    var processedRuns = runs

    // Skip type specifier if requested
    if (skipTypeSpec && runs.size > 2) {
        val firstRun = parseSongRun(runs[0])
        val secondRun = runs[1]
        val thirdRun = parseSongRun(runs[2])

        if (firstRun["type"] == "artist" &&
            secondRun == DOT_SEPARATOR_RUN &&
            thirdRun["type"] == "artist"
        ) {
            processedRuns = runs.drop(2)
        }
    }

    processedRuns.forEachIndexed { i, run ->
        if (i % 2 == 1) return@forEachIndexed // Skip separators

        val parsedRun = parseSongRun(run)
        val data = parsedRun["data"]
        val type = parsedRun["type"] as String

        when (type) {
            "album" -> parsed["album"] = data
            "artist" -> {
                val artists =
                    parsed.getOrPut("artists") { mutableListOf<Map<String, Any?>>() } as MutableJsonList
                artists.add(data as JsonDict)
            }

            "views" -> parsed["views"] = data
            "duration" -> {
                parsed["duration"] = data
                parsed["duration_seconds"] = parseDuration(data as String)
            }

            "year" -> parsed["year"] = data
        }
    }

    return parsed
}

fun parseSongRun(run: JsonDict): Map<String, Any?> {
    val text = run["text"] as? String ?: return mapOf("type" to "unknown", "data" to null)

    if (run.containsKey("navigationEndpoint")) {
        val item = mutableMapOf(
            "name" to text,
            "id" to navigatePath(run, NAVIGATION_BROWSE_ID, true)
        )

        val id = item["id"] as? String
        return if (id?.startsWith("MPRE") == true || id?.contains("release_detail") == true) {
            mapOf("type" to "album", "data" to item)
        } else {
            mapOf("type" to "artist", "data" to item)
        }
    } else {
        // Parse other types
        return when {
            Pattern.matches("^\\d([^ ])* [^ ]*$", text) -> {
                mapOf("type" to "views", "data" to text.split(" ")[0])
            }

            Pattern.matches("^(\\d+:)*\\d+:\\d+$", text) -> {
                mapOf("type" to "duration", "data" to text)
            }

            Pattern.matches("^\\d{4}$", text) -> {
                mapOf("type" to "year", "data" to text)
            }

            else -> {
                mapOf("type" to "artist", "data" to mapOf("name" to text, "id" to null))
            }
        }
    }
}
