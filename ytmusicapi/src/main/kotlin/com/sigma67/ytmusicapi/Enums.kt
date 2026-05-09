package com.sigma67.ytmusicapi

/**
 * Enums for ytmusicapi
 */

enum class ResponseStatus(val value: String) {
    SUCCEEDED("STATUS_SUCCEEDED")
}

enum class AuthType {
    UNAUTHORIZED,
    BROWSER,
    OAUTH_CUSTOM_CLIENT,
    OAUTH_CUSTOM_FULL
}

enum class LikeStatus(val value: String) {
    LIKE("LIKE"),
    DISLIKE("DISLIKE"),
    INDIFFERENT("INDIFFERENT")
}
