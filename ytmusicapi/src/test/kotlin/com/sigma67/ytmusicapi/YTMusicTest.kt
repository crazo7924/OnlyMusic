package com.sigma67.ytmusicapi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class YTMusicTest {

    @Test
    fun testYTMusicInitialization() {
        val ytMusic = YTMusic()
        assertNotNull(ytMusic)
        assertEquals(AuthType.UNAUTHORIZED, ytMusic.authType)
    }

    @Test
    fun testYTMusicWithLanguage() {
        val ytMusic = YTMusic(language = "en")
        assertNotNull(ytMusic)
        assertEquals("en", ytMusic.language)
    }

    @Test
    fun testYTMusicInvalidLanguage() {
        assertThrows<YTMusicUserError> {
            YTMusic(language = "invalid")
        }
    }

    @Test
    fun testYTMusicInvalidLocation() {
        assertThrows<YTMusicUserError> {
            YTMusic(location = "INVALID")
        }
    }

    @Test
    fun testYTMusicValidLocation() {
        val ytMusic = YTMusic(location = "US")
        assertNotNull(ytMusic)
    }
}

class ConstantsTest {

    @Test
    fun testSupportedLanguages() {
        assert(SUPPORTED_LANGUAGES.contains("en"))
        assert(SUPPORTED_LANGUAGES.contains("es"))
        assert(SUPPORTED_LANGUAGES.contains("fr"))
    }

    @Test
    fun testSupportedLocations() {
        assert(SUPPORTED_LOCATIONS.contains("US"))
        assert(SUPPORTED_LOCATIONS.contains("GB"))
        assert(SUPPORTED_LOCATIONS.contains("DE"))
    }
}

class HelpersTest {

    @Test
    fun testToInt() {
        assertEquals(123, toInt("123"))
        assertEquals(456, toInt("456"))
        assertEquals(0, toInt("abc"))
    }

    @Test
    fun testInitializeHeaders() {
        val headers = initializeHeaders()
        assert(headers.containsKey("user-agent"))
        assert(headers.containsKey("content-type"))
        assertEquals("application/json", headers["content-type"])
    }

    @Test
    fun testInitializeContext() {
        val context = initializeContext()
        assert(context.containsKey("context"))
    }
}

class AuthParseTest {

    @Test
    fun testDetermineAuthTypeUnauthorized() {
        val headers = emptyMap<String, String>()
        val authType = determineAuthType(headers)
        assertEquals(AuthType.UNAUTHORIZED, authType)
    }

    @Test
    fun testDetermineAuthTypeBrowser() {
        val headers = mapOf("authorization" to "SAPISIDHASH 12345_hash")
        val authType = determineAuthType(headers)
        assertEquals(AuthType.BROWSER, authType)
    }
}

class ExceptionsTest {

    @Test
    fun testYTMusicError() {
        val error = YTMusicError("Test error")
        assertEquals("Test error", error.message)
    }

    @Test
    fun testYTMusicUserError() {
        val error = YTMusicUserError("User error")
        assertEquals("User error", error.message)
    }

    @Test
    fun testYTMusicServerError() {
        val error = YTMusicServerError("Server error")
        assertEquals("Server error", error.message)
    }
}
