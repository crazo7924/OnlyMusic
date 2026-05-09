package com.sigma67.ytmusicapi.models.lyrics

import com.sigma67.ytmusicapi.JsonDict
import kotlinx.serialization.Serializable

@Serializable
data class LyricLine(
    val text: String,
    val startTime: Int,
    val endTime: Int,
    val id: Int,
) {
    companion object {
        fun fromRaw(rawLyric: JsonDict): LyricLine {
            val text = rawLyric["lyricLine"] as String

            @Suppress("UNCHECKED_CAST")
            val cueRange = rawLyric["cueRange"] as Map<String, Any?>
            val startTime = (cueRange["startTimeMilliseconds"] as? Number)?.toInt() ?: 0
            val endTime = (cueRange["endTimeMilliseconds"] as? Number)?.toInt() ?: 0

            @Suppress("UNCHECKED_CAST")
            val metadata = cueRange["metadata"] as? Map<String, Any?> ?: emptyMap()
            val id = (metadata["id"] as? Number)?.toInt() ?: 0

            return LyricLine(text, startTime, endTime, id)
        }
    }
}