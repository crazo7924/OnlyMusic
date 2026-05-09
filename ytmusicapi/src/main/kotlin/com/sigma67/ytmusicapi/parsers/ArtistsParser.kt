package com.sigma67.ytmusicapi.parsers

import com.sigma67.ytmusicapi.JsonDict
import com.sigma67.ytmusicapi.JsonList
import com.sigma67.ytmusicapi.NAVIGATION_BROWSE_ID
import com.sigma67.ytmusicapi.THUMBNAILS
import com.sigma67.ytmusicapi.TITLE_TEXT
import com.sigma67.ytmusicapi.navigatePath

/**
 * Artists parser implementation
 */

object ArtistsParser {
    fun parseArtists(data: JsonList, uploaded: Boolean = false): JsonList {
        return data.mapNotNull { item ->
            try {
                val itemMap = item as JsonDict
                val artist = mutableMapOf<String, Any?>()

                artist["name"] = navigatePath(itemMap, TITLE_TEXT, true)
                artist["browseId"] = navigatePath(itemMap, NAVIGATION_BROWSE_ID, true)
                artist["thumbnails"] = navigatePath(itemMap, THUMBNAILS, true)

                val subtitleRuns =
                    navigatePath(itemMap, listOf("subtitle", "runs"), true) as? JsonList
                if (!subtitleRuns.isNullOrEmpty()) {
                    val subscriberText = subtitleRuns[0]["text"] as? String
                    artist["subscribers"] = subscriberText?.split(" ")?.get(0)
                }

                if (uploaded) {
                    artist["songs"] =
                        navigatePath(itemMap, listOf("songs", "runs", 0, "text"), true)
                }

                artist
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parseArtistsRuns(runs: JsonList): JsonList {
        val artists = mutableListOf<JsonDict>()
        val step = 2 // Skip every other run to avoid separators

        for (i in 0 until runs.size step step) {
            try {
                val run = runs[i]
                val artist = mapOf(
                    "name" to run["text"],
                    "id" to navigatePath(run, NAVIGATION_BROWSE_ID, true)
                )
                artists.add(artist)
            } catch (e: Exception) {
                // Skip invalid runs
            }
        }

        return artists
    }
}