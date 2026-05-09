package com.sigma67.ytmusicapi

/**
 * Localization and internationalization support
 */

interface Translator {
    fun translate(key: String): String
    fun getLanguage(): String
}

class DefaultTranslator(private val language: String) : Translator {
    override fun translate(key: String): String {
        // Default translations
        return when (key) {
            "SEARCH" -> if (language == "es") "Buscar" else "Search"
            "LIBRARY" -> if (language == "es") "Biblioteca" else "Library"
            "PLAYLIST" -> if (language == "es") "Lista de reproducción" else "Playlist"
            else -> key
        }
    }

    override fun getLanguage(): String = language
}

class I18NParser(language: String) {
    private val translator = DefaultTranslator(language)

    fun translate(key: String): String {
        return translator.translate(key)
    }
}
