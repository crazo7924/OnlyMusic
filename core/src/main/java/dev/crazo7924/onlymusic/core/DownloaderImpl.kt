/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.core

import okhttp3.OkHttpClient
import okhttp3.Request.*
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit

// source: https://github.com/TeamNewPipe/NewPipe/blob/7cecda5713c3a9c493f7fbc0bc74f61483954a95/app/src/main/java/org/schabi/newpipe/DownloaderImpl.java
object DownloaderImpl : Downloader() {

    // Cookies are constant, precompute the string to avoid unnecessary allocations and iteration
    private val precomputedCookies = buildString {
        // Recaptcha cookie is always added
        // TODO: not sure if this is necessary
        append(RECAPTCHA_COOKIES_KEY).append("=")
        append("; ")
        append(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY).append("=").append(YOUTUBE_RESTRICTED_MODE_COOKIE)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .readTimeout(
            30,
            TimeUnit.SECONDS
        ).build()

    private fun getCookies(): String {
        return precomputedCookies
    }

    /**
     * Get the size of the content that the url is pointing by firing a HEAD request.
     *
     * @param url an url pointing to the content
     * @return the size of the content, in bytes
     */
    @Throws(IOException::class)
    fun getContentLength(url: String?): Long {
        try {
            val response = head(url)
            return response.getHeader("Content-Length")!!.toLong()
        } catch (e: NumberFormatException) {
            throw IOException("Invalid content length", e)
        } catch (e: ReCaptchaException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod: String = request.httpMethod()
        val url: String = request.url()
        val headers: MutableMap<String, MutableList<String>> = request.headers()
        val dataToSend: ByteArray? = request.dataToSend()

        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = dataToSend.toRequestBody()
        }

        val requestBuilder: Builder = Builder()
            .method(httpMethod, requestBody)
            .url(url)
            .addHeader("User-Agent", USER_AGENT)

        val cookies = getCookies()
        if (cookies.isNotEmpty()) {
            requestBuilder.addHeader("Cookie", cookies)
        }

        for ((headerName, headerValueList) in headers) {
            requestBuilder.removeHeader(headerName)
            for (i in 0 until headerValueList.size) {
                requestBuilder.addHeader(
                    headerName,
                    headerValueList[i]
                )
            }
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("reCaptcha Challenge requested", url)
            }
            var responseBodyToReturn: String? = null
            response.body.use { body ->
                responseBodyToReturn = body?.string()
            }
            val latestUrl: String = response.request.url.toString()
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                responseBodyToReturn,
                latestUrl
            )
        }
    }
}

private const val RECAPTCHA_COOKIES_KEY = "recaptcha_cookies"
private const val YOUTUBE_RESTRICTED_MODE_COOKIE: String = "PREF=f2=8000000"
private const val YOUTUBE_RESTRICTED_MODE_COOKIE_KEY: String = "youtube_restricted_mode_key"
private const val USER_AGENT: String =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"