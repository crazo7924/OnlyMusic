package com.sigma67.ytmusicapi

/**
 * Navigation paths for API response traversal.
 * Organized by category for better maintainability.
 */

// -- Structure & Containers --
val CONTENT = listOf("contents", 0)
val TAB_CONTENT = listOf("tabs", 0, "tabRenderer", "content")
val TWO_COLUMN_RENDERER = listOf("contents", "twoColumnBrowseResultsRenderer")
val SINGLE_COLUMN_TAB = listOf("contents", "singleColumnBrowseResultsRenderer") + TAB_CONTENT
val SECTION_LIST = listOf("sectionListRenderer", "contents")
val SECTION_LIST_ITEM = listOf("sectionListRenderer", "contents", 0)
val RESPONSIVE_HEADER = listOf("musicResponsiveHeaderRenderer")
val MUSIC_SHELF = listOf("musicShelfRenderer")

// -- Text Content --
val RUN_TEXT = listOf("runs", 0, "text")
val TITLE_TEXT = listOf("title") + RUN_TEXT
val SUBTITLE = listOf("subtitle") + RUN_TEXT
val SUBTITLE2 = listOf("subtitle", "runs", 2, "text")
val DESCRIPTION = listOf("description") + RUN_TEXT
val TEXT_RUNS = listOf("text", "runs")
val TEXT_RUN = listOf("text", "runs", 0)
val SUBTITLE_RUNS = listOf("subtitle", "runs")

// -- Thumbnails & Badges --
val THUMBNAIL = listOf("thumbnail", "thumbnails")
val THUMBNAILS = listOf("thumbnail", "musicThumbnailRenderer", "thumbnail", "thumbnails")
val BADGE_PATH =
    listOf(0, "musicInlineBadgeRenderer", "accessibilityData", "accessibilityData", "label")
val BADGE_LABEL = listOf("badges") + BADGE_PATH
val SUBTITLE_BADGE_LABEL = listOf("subtitleBadges") + BADGE_PATH

// -- Endpoints --
val NAVIGATION_BROWSE_ID = listOf("navigationEndpoint", "browseEndpoint", "browseId")
val WATCH_VIDEO_ID = listOf("watchEndpoint", "videoId")
val WATCH_PLAYLIST_ID = listOf("watchEndpoint", "playlistId")
val WATCH_PID = listOf("watchPlaylistEndpoint", "playlistId")
val NAVIGATION_WATCH_PLAYLIST_ID = listOf("navigationEndpoint") + WATCH_PID
val NAVIGATION_VIDEO_TYPE = listOf(
    "watchEndpoint",
    "watchEndpointMusicSupportedConfigs",
    "watchEndpointMusicConfig",
    "musicVideoType"
)

// -- Menu & Actions --
val MENU_ITEMS = listOf("menu", "menuRenderer", "items")
val MENU_LIKE_STATUS =
    listOf("menu", "menuRenderer", "topLevelButtons", 0, "likeButtonRenderer", "likeStatus")
const val MNIR = "menuNavigationItemRenderer"
const val TOGGLE_MENU = "toggleMenuServiceItemRenderer"
val ICON_TYPE = listOf("icon", "iconType")
val MENU_PLAYLIST_ID = MENU_ITEMS + listOf(0, MNIR) + NAVIGATION_WATCH_PLAYLIST_ID

// -- Overlays & Misc --
val OVERLAY_RENDERER =
    listOf("musicItemThumbnailOverlayRenderer", "content", "musicPlayButtonRenderer")
val PLAY_BUTTON = listOf("overlay") + OVERLAY_RENDERER
val THUMBNAIL_OVERLAY_NAVIGATION =
    listOf("thumbnailOverlay") + OVERLAY_RENDERER + listOf("playNavigationEndpoint")
val HEADER = listOf("header")
val CARD_SHELF_TITLE = HEADER + listOf("musicCardShelfHeaderBasicRenderer") + TITLE_TEXT
val DESCRIPTION_SHELF = listOf("musicDescriptionShelfRenderer")
