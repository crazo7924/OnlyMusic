package dev.crazo7924.onlymusic.player

enum class PlayerCmd {
    PLAY_PAUSE,
    NEXT,
    PREV,
    STOP,
    SEEK_TO,
    UNSET;

    companion object {
        fun fromInt(value: Int): PlayerCmd =
            PlayerCmd.entries.firstOrNull { it.ordinal == value } ?: UNSET
    }
}
