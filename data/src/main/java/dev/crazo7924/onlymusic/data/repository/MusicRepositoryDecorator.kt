package dev.crazo7924.onlymusic.data.repository

/**
 * A base decorator for the [MusicRepository] interface.
 * This class provides a foundation for adding cross-cutting concerns like caching or logging
 * to any [MusicRepository] implementation.
 *
 * It uses Kotlin's interface delegation feature to forward all calls to the
 * [decorated] repository by default. Subclasses should override specific methods
 * to introduce new behavior.
 *
 * @property decorated The actual [MusicRepository] instance that this class wraps.
 */
open class MusicRepositoryDecorator(
    private val decorated: MusicRepository,
) : MusicRepository by decorated