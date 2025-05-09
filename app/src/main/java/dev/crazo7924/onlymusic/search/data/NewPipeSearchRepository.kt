package dev.crazo7924.onlymusic.search.data

import android.util.Log
import dev.crazo7924.onlymusic.DownloaderImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import java.io.IOException

class NewPipeSearchRepository : SearchRepository {

    init {
        NewPipe.init(DownloaderImpl)
    }

    override suspend fun search(query: String): Result<List<SearchSuggestion>> {
        return withContext(Dispatchers.IO) {
            val linkHandler = YoutubeSearchQueryHandlerFactory.getInstance()
                .fromQuery(query, MUSIC_CONTENT_FILTERS, "")

            val extractor = YoutubeMusicSearchExtractor(
                ServiceList.YouTube,
                linkHandler
            )

            Log.d(TAG, "search: About to call fetchPage()")
            try {
                extractor.fetchPage()
            } catch (ioException: IOException) {
                Log.e(TAG, "search error: $ioException")
                Result.failure<Exception>(ioException)
            } catch (extractorException: ExtractionException) {
                Log.e(TAG, "search error: $extractorException")
                Result.failure<Exception>(extractorException)
            }
            Log.d(TAG, "search: ${extractor.initialPage.items.size}")
            Result.success(extractor.initialPage.items.map {
                SearchSuggestion(
                    it.name,
                    it.infoType.toTypeString(),
                    it.thumbnails.last().url
                )
            })
        }
    }


    companion object {
        private const val TAG = "NewPipe Search"
        private val MUSIC_CONTENT_FILTERS = listOf(
            YoutubeSearchQueryHandlerFactory.MUSIC_SONGS,
            YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS,
            YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS,
            YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS
        )
    }

    private fun InfoType.toTypeString(): String {
        return when (this) {
            InfoType.STREAM -> "Song"
            InfoType.PLAYLIST -> "Album"
            InfoType.CHANNEL -> "Artist"
            InfoType.COMMENT -> "Comment (wtf)"
        }
    }

}