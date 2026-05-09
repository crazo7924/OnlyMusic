package com.sigma67.ytmusicapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * HTTP client interface for making requests
 */
interface HttpClient {
    fun post(
        url: String,
        headers: Map<String, String>,
        body: String,
        cookies: Map<String, String> = emptyMap(),
    ): String

    fun get(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String> = emptyMap(),
        cookies: Map<String, String> = emptyMap(),
    ): String
}

/**
 * OkHttp-based HTTP client implementation
 */
class OkHttpClientImpl(private val client: OkHttpClient = createDefaultClient()) : HttpClient {
    override fun post(
        url: String,
        headers: Map<String, String>,
        body: String,
        cookies: Map<String, String>,
    ): String {
        val mediaType = "application/json".toMediaType()
        val requestBody = body.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // Add cookies
        val cookieLine = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
        if (cookieLine.isNotEmpty()) {
            requestBuilder.addHeader("Cookie", cookieLine)
        }

        val response = client.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: "{}"
    }

    override fun get(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>,
        cookies: Map<String, String>,
    ): String {
        var finalUrl = url
        if (params.isNotEmpty()) {
            val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            finalUrl = "$url?$queryString"
        }

        val requestBuilder = Request.Builder()
            .url(finalUrl)
            .get()

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // Add cookies
        val cookieLine = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
        if (cookieLine.isNotEmpty()) {
            requestBuilder.addHeader("Cookie", cookieLine)
        }

        val response = client.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: "{}"
    }

    companion object {
        fun createDefaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}

/**
 * Base class for YouTube Music API interactions
 */
open class YTMusicBase(
    auth: String? = null,
    user: String? = null,
    httpClient: HttpClient? = null,
    var proxies: Map<String, String>? = null,
    var language: String = "en",
    location: String = "",
    oauthCredentials: com.sigma67.ytmusicapi.auth.OAuthCredentials? = null,
) {
    protected var session: HttpClient = httpClient ?: OkHttpClientImpl()
    var cookies: MutableMap<String, String> = mutableMapOf("SOCS" to "CAI")

    var authHeaders: MutableMap<String, String> = mutableMapOf()
    var authType: AuthType = AuthType.UNAUTHORIZED

    var sapisid: String = ""
    var origin: String = ""

    var context: MutableJsonDict = (initializeContext().toMutableMap())
    var params: String = YTM_PARAMS

    init {
        validateLanguage(language)
        validateLocation(location)

        if (location.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            val contextMap = context["context"] as? MutableJsonDict
            val clientMap = contextMap?.get("client") as? MutableJsonDict
            clientMap?.put("gl", location)
        }

        @Suppress("UNCHECKED_CAST")
        val contextMap = context["context"] as? MutableJsonDict
        val clientMap = contextMap?.get("client") as? MutableJsonDict
        clientMap?.put("hl", language)

        if (auth != null) {
            val (headers, _) = com.sigma67.ytmusicapi.auth.parseAuthStr(auth)
            authHeaders = headers.toMutableMap()
            authType = com.sigma67.ytmusicapi.auth.determineAuthType(authHeaders)

            if (authType == AuthType.OAUTH_CUSTOM_CLIENT) {
                require(oauthCredentials != null) {
                    "oauth JSON provided via auth argument, but oauth_credentials not provided"
                }
                // Initialize OAuth token handling
            }
        }

        if (authType == AuthType.BROWSER) {
            params += YTM_PARAMS_KEY
            try {
                val cookie = authHeaders["cookie"] ?: authHeaders["Cookie"] ?: ""
                handleCookie(cookie)
            } catch (e: Exception) {
                throw YTMusicUserError(
                    "Your cookie is missing the required value __Secure-3PAPISID",
                    e
                )
            }
        }
    }

    private fun handleCookie(rawCookie: String) {
        sapisid = sapisidFromCookie(rawCookie)
        origin = authHeaders["origin"] ?: authHeaders["x-origin"] ?: ""
    }

    private fun validateLanguage(language: String) {
        if (language !in SUPPORTED_LANGUAGES) {
            throw YTMusicUserError(
                "Language not supported. Supported languages are " + SUPPORTED_LANGUAGES.joinToString(
                    ", "
                )
            )
        }
    }

    private fun validateLocation(location: String) {
        if (location.isNotEmpty() && location !in SUPPORTED_LOCATIONS) {
            throw YTMusicUserError("Location not supported. Check the FAQ for supported locations.")
        }
    }

    fun baseHeaders(): Map<String, String> {
        val headers = if (authType == AuthType.BROWSER || authType == AuthType.OAUTH_CUSTOM_FULL) {
            authHeaders.toMap()
        } else {
            initializeHeaders()
        }

        val mutableHeaders = headers.toMutableMap()
        if ("X-Goog-Visitor-Id" !in mutableHeaders) {
            val visitorId = getVisitorId { url -> session.get(url, mutableHeaders) }
            mutableHeaders.putAll(visitorId)
        }

        return mutableHeaders
    }

    fun headers(): Map<String, String> {
        val headers = baseHeaders().toMutableMap()

        if (authType == AuthType.BROWSER) {
            headers["authorization"] = getAuthorization("$sapisid $origin")
        } else if (authType == AuthType.OAUTH_CUSTOM_CLIENT) {
            // Add OAuth token
            headers["X-Goog-Request-Time"] = (System.currentTimeMillis() / 1000).toString()
        }

        return headers
    }

    protected fun sendRequest(
        endpoint: String,
        body: JsonDict,
        additionalParams: String = "",
    ): JsonDict {
        val mutableBody = body.toMutableMap()
        mutableBody.putAll(context)

        val url = YTM_BASE_API + endpoint + params + additionalParams
        val headers = headers()
        val json = Json.encodeToString(mutableBody)

        return try {
            val response = session.post(url, headers, json, cookies)
            parseJsonResponse(response)
        } catch (e: Exception) {
            throw YTMusicServerError("Failed to send request: ${e.message}", e)
        }
    }

    protected fun sendGetRequest(
        url: String,
        params: JsonDict? = null,
        useBaseHeaders: Boolean = false,
    ): String {
        val headers = if (useBaseHeaders) initializeHeaders() else headers()
        return session.get(
            url,
            headers,
            (params ?: emptyMap()).mapValues { it.value.toString() },
            cookies
        )
    }

    protected fun checkAuth() {
        if (authType == AuthType.UNAUTHORIZED) {
            throw YTMusicUserError("Please provide authentication before using this function")
        }
    }

    private fun parseJsonResponse(response: String): JsonDict {
        return try {
            val jsonElement = Json.parseToJsonElement(response)
            @Suppress("UNCHECKED_CAST")
            jsonElement.jsonObject.toMap() as JsonDict
        } catch (e: Exception) {
            throw YTMusicServerError("Invalid JSON response from server", e)
        }
    }
}

/**
 * Main YouTube Music API client
 */
class YTMusic(
    auth: String? = null,
    user: String? = null,
    httpClient: HttpClient? = null,
    proxies: Map<String, String>? = null,
    language: String = "en",
    location: String = "",
    oauthCredentials: com.sigma67.ytmusicapi.auth.OAuthCredentials? = null,
) : YTMusicBase(auth, user, httpClient, proxies, language, location, oauthCredentials) {

    /**
     * Execute operations in mobile context
     */
    fun <T> asMobile(block: (YTMusic) -> T): T {
        @Suppress("UNCHECKED_CAST")
        val contextMap = context["context"] as? MutableMap<String, Any?>
        val clientMap = contextMap?.get("client") as? MutableMap<String, Any?>

        val originalClientName = clientMap?.get("clientName")
        val originalClientVersion = clientMap?.get("clientVersion")

        clientMap?.put("clientName", "ANDROID_MUSIC")
        clientMap?.put("clientVersion", "7.21.50")

        return try {
            block(this)
        } finally {
            clientMap?.put("clientName", originalClientName ?: "WEB_REMIX")
            clientMap?.put("clientVersion", originalClientVersion ?: "1.0")
        }
    }

    // Stub methods for mixin functionality that will be expanded
    fun search(query: String): JsonList {
        TODO("Search implementation")
    }

    fun getBrowsingHome(): JsonList {
        TODO("Home browsing implementation")
    }

    fun getPlaylist(playlistId: String): JsonDict {
        TODO("Get playlist implementation")
    }

    fun getAlbum(albumId: String): JsonDict {
        TODO("Get album implementation")
    }
}
