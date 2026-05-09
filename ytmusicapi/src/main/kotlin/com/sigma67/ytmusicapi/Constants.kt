package com.sigma67.ytmusicapi

/**
 * Constants for YouTube Music API
 */

const val YTM_DOMAIN = "https://music.youtube.com"
const val YTM_BASE_API = "$YTM_DOMAIN/youtubei/v1/"
const val YTM_PARAMS = "?alt=json"
const val YTM_PARAMS_KEY = "&key=AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0"
const val OAUTH_USER_AGENT = "$USER_AGENT Cobalt/Version"

// API Endpoints
const val BROWSE_ENDPOINT = "browse"
const val SEARCH_ENDPOINT = "search"
const val NEXT_ENDPOINT = "next"
const val GET_SEARCH_SUGGESTIONS_ENDPOINT = "get_search_suggestions"
const val CREATE_PLAYLIST_ENDPOINT = "create_playlist"
const val DELETE_PLAYLIST_ENDPOINT = "delete_playlist"
const val ADD_PLAYLIST_ITEMS_ENDPOINT = "add_playlist_items"
const val REMOVE_PLAYLIST_ITEMS_ENDPOINT = "remove_playlist_items"
const val EDIT_PLAYLIST_ENDPOINT = "edit_playlist"
const val LIKE_ENDPOINT = "like"
const val SUBSCRIBE_ENDPOINT = "subscribe"
const val UNSUBSCRIBE_ENDPOINT = "unsubscribe"
const val ACCOUNT_MENU_ENDPOINT = "account_menu"
const val GUIDE_ENDPOINT = "guide"

// Browse IDs
const val HOME_BROWSE_ID = "FEmusic_home"
const val EXPLORE_BROWSE_ID = "FEmusic_explore"
const val CHARTS_BROWSE_ID = "FEmusic_charts"

val SUPPORTED_LANGUAGES = setOf(
    "ar",
    "cs",
    "de",
    "en",
    "es",
    "fr",
    "hi",
    "it",
    "ja",
    "ko",
    "nl",
    "pt",
    "ru",
    "tr",
    "ur",
    "zh_CN",
    "zh_TW"
)

val SUPPORTED_LOCATIONS = setOf(
    "AE", "AR", "AT", "AU", "AZ", "BA", "BD", "BE", "BG", "BH", "BO", "BR", "BY", "CA", "CH", "CL",
    "CO", "CR", "CY", "CZ", "DE", "DK", "DO", "DZ", "EC", "EE", "EG", "ES", "FI", "FR", "GB", "GE",
    "GH", "GR", "GT", "HK", "HN", "HR", "HU", "ID", "IE", "IL", "IN", "IQ", "IS", "IT", "JM", "JO",
    "JP", "KE", "KH", "KR", "KW", "KZ", "LA", "LB", "LI", "LK", "LT", "LU", "LV", "LY", "MA", "ME",
    "MK", "MT", "MX", "MY", "NG", "NI", "NL", "NO", "NP", "NZ", "OM", "PA", "PE", "PG", "PH", "PK",
    "PL", "PR", "PT", "PY", "QA", "RO", "RS", "RU", "SA", "SE", "SG", "SI", "SK", "SN", "SV", "TH",
    "TN", "TR", "TW", "TZ", "UA", "UG", "US", "UY", "VE", "VN", "YE", "ZA", "ZW"
)

const val OAUTH_SCOPE = "https://www.googleapis.com/auth/youtube"
const val OAUTH_CODE_URL = "https://www.youtube.com/o/oauth2/device/code"
const val OAUTH_TOKEN_URL = "https://oauth2.googleapis.com/token"
