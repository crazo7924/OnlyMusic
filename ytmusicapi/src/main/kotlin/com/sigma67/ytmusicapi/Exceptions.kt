package com.sigma67.ytmusicapi

/**
 * Custom exception classes for ytmusicapi
 */

/**
 * Base error class. Should only be raised if none of the subclasses are fitting.
 */
open class YTMusicError(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Error caused by invalid usage of ytmusicapi
 */
class YTMusicUserError(message: String, cause: Throwable? = null) : YTMusicError(message, cause)

/**
 * Error caused by the YouTube Music backend
 */
class YTMusicServerError(message: String, cause: Throwable? = null) : YTMusicError(message, cause)
