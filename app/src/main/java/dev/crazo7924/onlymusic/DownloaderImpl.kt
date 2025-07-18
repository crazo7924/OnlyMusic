package dev.crazo7924.onlymusic

import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.Arrays
import java.util.Objects
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

// source: https://github.com/TeamNewPipe/NewPipe/blob/7cecda5713c3a9c493f7fbc0bc74f61483954a95/app/src/main/java/org/schabi/newpipe/DownloaderImpl.java
object DownloaderImpl : Downloader() {

    private val mCookies: MutableMap<String?, String?> = HashMap()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(
            30,
            TimeUnit.SECONDS
        ).build()

    private fun getCookies(url: String): String {
        val youtubeCookie = if (url.contains(YOUTUBE_DOMAIN))
            getCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY)
        else
            null

        // Recaptcha cookie is always added TODO: not sure if this is necessary
        return Stream.of(youtubeCookie, getCookie(RECAPTCHA_COOKIES_KEY))
            .filter(Objects::nonNull)
            .flatMap { cookies -> Arrays.stream(cookies!!.split("; *").toTypedArray()) }
            .distinct()
            .collect(Collectors.joining("; "))
    }

    private fun getCookie(key: String?): String? {
        return mCookies[key]
    }

    private fun setCookie(key: String?, cookie: String?) {
        mCookies[key] = cookie
    }

    private fun removeCookie(key: String?) {
        mCookies.remove(key)
    }

    fun updateYoutubeRestrictedModeCookies(youtubeRestrictedModeEnabled: Boolean) {
        if (youtubeRestrictedModeEnabled) {
            setCookie(
                YOUTUBE_RESTRICTED_MODE_COOKIE_KEY,
                YOUTUBE_RESTRICTED_MODE_COOKIE
            )
        } else {
            removeCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY)
        }
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

        val cookies = getCookies(url)
        if (cookies.isNotEmpty()) {
            requestBuilder.addHeader("Cookie", cookies)
        }

        headers.forEach { (headerName: String, headerValueList: MutableList<String>) ->
            requestBuilder.removeHeader(headerName)
            headerValueList.forEach(Consumer { headerValue: String ->
                requestBuilder.addHeader(
                    headerName,
                    headerValue
                )
            })
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("reCaptcha Challenge requested", url)
            }
            var responseBodyToReturn: String? = null
            response.body.use { body ->
                responseBodyToReturn = body.string()
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
private const val YOUTUBE_DOMAIN: String = "youtube.com"
private const val YOUTUBE_RESTRICTED_MODE_COOKIE: String = "PREF=f2=8000000"
private const val YOUTUBE_RESTRICTED_MODE_COOKIE_KEY: String = "youtube_restricted_mode_key"
private const val USER_AGENT: String =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"
private const val TAG = "DownloaderImpl"