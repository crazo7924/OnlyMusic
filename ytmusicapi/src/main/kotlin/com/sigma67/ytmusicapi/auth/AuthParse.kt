package com.sigma67.ytmusicapi.auth

import com.sigma67.ytmusicapi.AuthType
import com.sigma67.ytmusicapi.YTMusicUserError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Authentication parsing for ytmusicapi
 */

fun parseAuthStr(auth: String): Pair<Map<String, String>, Path?> {
    var authPath: Path? = null
    val headers = mutableMapOf<String, String>()

    try {
        when {
            auth.startsWith("{") -> {
                // Direct JSON string
                parseJsonString(auth, headers)
            }

            Path.of(auth).toFile().isFile -> {
                // File path
                authPath = Path.of(auth)
                val content = Files.readString(authPath)
                parseJsonString(content, headers)
            }

            else -> {
                throw YTMusicUserError("Invalid auth JSON string or file path provided.")
            }
        }
    } catch (e: Exception) {
        if (e is YTMusicUserError) throw e
        throw YTMusicUserError("Invalid auth JSON string or file path provided.", e)
    }

    return headers to authPath
}

private fun parseJsonString(jsonString: String, headers: MutableMap<String, String>) {
    try {
        val jsonElement = Json.parseToJsonElement(jsonString)
        val jsonObject = jsonElement.jsonObject

        jsonObject.forEach { (key, value) ->
            val stringValue = value.toString().replace("\"", "")
            headers[key] = URLDecoder.decode(stringValue, "UTF-8")
        }
    } catch (e: Exception) {
        throw YTMusicUserError("Failed to parse JSON from auth string", e)
    }
}

fun determineAuthType(authHeaders: Map<String, String>): AuthType {
    var authType = AuthType.UNAUTHORIZED

    if (OAuthToken.isOAuth(authHeaders)) {
        authType = AuthType.OAUTH_CUSTOM_CLIENT
    }

    val authorization = authHeaders.entries.find { (key, _) ->
        key.equals("authorization", ignoreCase = true)
    }?.value

    if (authorization != null) {
        when {
            "SAPISIDHASH" in authorization -> authType = AuthType.BROWSER
            authorization.startsWith("Bearer") -> authType = AuthType.OAUTH_CUSTOM_FULL
        }
    }

    return authType
}
