package com.sigma67.ytmusicapi.auth

/**
 * OAuth exceptions
 */

open class OAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

class OAuthExpiredException(message: String, cause: Throwable? = null) :
    OAuthException(message, cause)

class OAuthNotSupportedException(message: String, cause: Throwable? = null) :
    OAuthException(message, cause)
