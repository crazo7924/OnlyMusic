package com.sigma67.ytmusicapi.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OAuth token types
 */

@Serializable
data class OAuthToken(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String = "Bearer",
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("scope")
    val scope: String? = null,
) {
    fun isExpired(): Boolean {
        // Implementation would check if token is expired
        return false
    }

    companion object {
        fun isOAuth(headers: Map<String, String>): Boolean {
            // Check if headers contain OAuth tokens
            return headers.any { (key, value) ->
                key.equals("authorization", ignoreCase = true) && value.startsWith("Bearer")
            } || headers.containsKey("access_token")
        }
    }
}

/**
 * Refreshing token that automatically renews when expired
 */
class RefreshingToken(
    val credentials: OAuthCredentials,
    initialToken: OAuthToken,
    private val localCache: java.nio.file.Path? = null,
) {
    private var token: OAuthToken = initialToken

    fun getToken(): String {
        if (token.isExpired()) {
            refresh()
        }
        return token.accessToken
    }

    fun getAuthHeaders(): Map<String, String> {
        return mapOf("Authorization" to "Bearer ${getToken()}")
    }

    private fun refresh() {
        // Implementation would refresh the token from OAuth provider
        // This would make HTTP request to OAUTH_TOKEN_URL
    }
}
