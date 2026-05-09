package com.sigma67.ytmusicapi.models.lyrics

data class TimedLyrics(
    override val lines: List<LyricLine>,
    override val source: String? = null,
) : Lyrics {
    override val hasTimestamps = true

    override fun toString(): String = "[TimedLyrics with ${lines.size} lines]"
}