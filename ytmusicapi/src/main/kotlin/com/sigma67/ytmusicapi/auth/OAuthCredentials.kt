package com.sigma67.ytmusicapi.auth

/**
 * OAuth credentials for custom OAuth client
 */
data class OAuthCredentials(
    val clientId: String,
    val clientSecret: String,
    val scope: String = "https://www.googleapis.com/auth/youtube",
)
