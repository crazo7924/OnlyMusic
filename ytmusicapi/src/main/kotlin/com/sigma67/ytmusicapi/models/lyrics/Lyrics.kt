package com.sigma67.ytmusicapi.models.lyrics

/**
 * Lyric models for ytmusicapi
 */

sealed interface Lyrics {
    val lines: List<LyricLine> // Renamed from lyrics to lines for clarity
    val source: String?
    val hasTimestamps: Boolean
}