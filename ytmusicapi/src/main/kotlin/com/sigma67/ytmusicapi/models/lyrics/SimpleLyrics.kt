package com.sigma67.ytmusicapi.models.lyrics

data class SimpleLyrics(
    override val lines: List<LyricLine>,
    override val source: String? = null,
) : Lyrics {
    // Secondary constructor to allow passing a single line easily
    constructor(line: LyricLine, source: String? = null) : this(listOf(line), source)

    override val hasTimestamps = false
}