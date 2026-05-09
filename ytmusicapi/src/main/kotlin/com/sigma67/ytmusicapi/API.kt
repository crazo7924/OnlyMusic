/**
 * ytmusicapi - Unofficial API for YouTube Music
 *
 * This is a Kotlin transpilation of the popular Python ytmusicapi library.
 * 
 * Main classes:
 * - YTMusic: Main API client class
 * - YTMusicBase: Base class with authentication and request handling
 *
 * Features:
 * - Browse YouTube Music content
 * - Search for songs, artists, albums, playlists
 * - Create and manage playlists
 * - Access library and uploads
 * - Watch and podcast support
 * - OAuth authentication support
 *
 * Example usage:
 * ```kotlin
 * val yt = YTMusic()
 * val results = yt.search("The Beatles")
 * ```
 *
 * Authentication:
 * ```kotlin
 * val yt = YTMusic(auth = "path/to/headers_auth.json")
 * ```
 */
package com.sigma67.ytmusicapi

