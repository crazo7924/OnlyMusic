package com.sigma67.ytmusicapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.nio.file.Files
import java.nio.file.Path

/**
 * Setup utilities for ytmusicapi
 */

/**
 * Setup authentication for ytmusicapi
 * Interactive setup for browser-based authentication
 */
fun setup(
    filepath: String = "headers_auth.json",
    headersData: JsonDict? = null,
) {
    if (headersData != null) {
        val path = Path.of(filepath)
        Files.createDirectories(path.parent ?: Path.of("."))

        val json = Json {
            prettyPrint = true
        }

        @Suppress("UNCHECKED_CAST")
        val jsonString = json.encodeToString(headersData as Map<String, Any?>)

        Files.writeString(path, jsonString)
        println("Headers saved to $filepath")
    }
}

/**
 * Setup OAuth authentication
 */
fun setupOAuth(
    clientId: String,
    clientSecret: String,
    filepath: String = "oauth_tokens.json",
) {
    val credentials = com.sigma67.ytmusicapi.auth.OAuthCredentials(
        clientId = clientId,
        clientSecret = clientSecret
    )

    println("OAuth credentials created. Use these in your YTMusic instance.")
    println("Client ID: ${credentials.clientId}")
}

/**
 * Load authentication from file
 */
fun loadAuth(filepath: String): JsonDict {
    val path = Path.of(filepath)
    if (!Files.exists(path)) {
        throw YTMusicUserError("Auth file not found at $filepath")
    }

    val content = Files.readString(path)
    val jsonElement = Json.parseToJsonElement(content)
    @Suppress("UNCHECKED_CAST")
    return jsonElement.jsonObject.toMap() as JsonDict
}

/**
 * Validate authentication headers
 */
fun validateHeaders(headers: JsonDict): Boolean {
    return headers.containsKey("authorization") ||
            headers.containsKey("cookie") ||
            headers.containsKey("access_token")
}
