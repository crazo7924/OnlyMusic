package dev.crazo7924.onlymusic.data.repository

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class NewPipeMusicRepositoryBenchmarkTest {

    @Test
    fun benchmarkLoadAutoPlaylistUri() = runBlocking {
        val repo = NewPipeMusicRepository()
        // A known youtube URL that has a mix playlist, or simply a valid track ID.
        // e.g. "https://music.youtube.com/watch?v=kJQP7kiw5Fk"
        val testUrl = "https://music.youtube.com/watch?v=kJQP7kiw5Fk"

        println("Starting baseline measurement for loadAutoPlaylistUri")
        val time = measureTimeMillis {
            val results = repo.loadAutoPlaylistUri(testUrl).toList()
            println("Loaded ${results.size} items.")
        }
        println("Baseline execution time: $time ms")
    }

    @Test
    fun benchmarkLoadPlaylistUri() = runBlocking {
        val repo = NewPipeMusicRepository()
        // A known playlist URL.
        val testUrl = "https://music.youtube.com/playlist?list=PL4fGSI1pDJn5kI81J1fYWK5eZRl1zJ5kM"

        println("Starting baseline measurement for loadPlaylistUri")
        val time = measureTimeMillis {
            val results = repo.loadPlaylistUri(testUrl).toList()
            println("Loaded ${results.size} items.")
        }
        println("Baseline execution time: $time ms")
    }
}
