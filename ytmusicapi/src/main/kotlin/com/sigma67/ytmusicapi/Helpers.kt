package com.sigma67.ytmusicapi

import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * Utility functions for ytmusicapi
 */

/**
 * Initialize headers for API requests
 */
fun initializeHeaders(): Map<String, String> {
    return mapOf(
        "user-agent" to USER_AGENT,
        "accept" to "*/*",
        "accept-encoding" to "gzip, deflate",
        "content-type" to "application/json",
        "content-encoding" to "gzip",
        "origin" to YTM_DOMAIN
    )
}

/**
 * Initialize context for API requests
 */
fun initializeContext(): JsonDict {
    val clientVersion = "1.${System.currentTimeMillis() / 1000 / 86400 + 18993}.01.00"
    return mapOf(
        "context" to mapOf(
            "client" to mapOf(
                "clientName" to "WEB_REMIX",
                "clientVersion" to clientVersion
            ),
            "user" to emptyMap<String, Any>()
        )
    )
}

/**
 * Get visitor ID from YouTube Music domain
 */
fun getVisitorId(requestFunc: (String) -> String): Map<String, String> {
    return try {
        val response = requestFunc(YTM_DOMAIN)
        val pattern = Pattern.compile("""ytcfg\.set\s*\(\s*(\{.+?})\s*\)\s*;""")
        val matcher = pattern.matcher(response)

        var visitorId = ""
        if (matcher.find()) {
            try {
                // Simple JSON parsing for extracting visitor data
                val jsonStr = matcher.group(1)
                val visitorPattern = Pattern.compile(""""VISITOR_DATA"\s*:\s*"([^"]*)" """)
                val visitorMatcher = visitorPattern.matcher(jsonStr)
                if (visitorMatcher.find()) {
                    visitorId = visitorMatcher.group(1)
                }
            } catch (e: Exception) {
                // ignore JSON parsing errors
            }
        }
        mapOf("X-Goog-Visitor-Id" to visitorId)
    } catch (e: Exception) {
        mapOf("X-Goog-Visitor-Id" to "")
    }
}

/**
 * Extract SAPISID from cookie
 */
fun sapisidFromCookie(rawCookie: String): String {
    return try {
        val pattern = Pattern.compile("""__Secure-3PAPISID\s*=\s*([^;]+)""")
        val matcher = pattern.matcher(rawCookie)
        if (matcher.find()) {
            matcher.group(1).replace("\"", "")
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get authorization header value
 * SAPISID Hash reverse engineered by https://stackoverflow.com/a/32065323/5726546
 */
fun getAuthorization(auth: String): String {
    val timestamp = (System.currentTimeMillis() / 1000).toString()
    val input = "$timestamp $auth"
    val digest = MessageDigest.getInstance("SHA-1")
    val hash = digest.digest(input.toByteArray()).joinToString("") { byte -> "%02x".format(byte) }
    return "SAPISIDHASH ${timestamp}_$hash"
}
