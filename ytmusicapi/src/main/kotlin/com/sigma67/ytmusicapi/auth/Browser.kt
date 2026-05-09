package com.sigma67.ytmusicapi.auth

import java.nio.file.Path

/**
 * Browser-based authentication
 */
class BrowserAuth {
    fun extractHeaders(): Map<String, String> {
        TODO("Browser authentication extraction not implemented")
    }
}

/**
 * Shared credentials between OAuth requests
 */
data class SharedCredentials(
    val clientId: String,
    val clientSecret: String,
    val tokenFile: Path? = null,
)
