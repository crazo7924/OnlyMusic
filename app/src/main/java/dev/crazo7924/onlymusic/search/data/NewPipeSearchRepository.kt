package dev.crazo7924.onlymusic.search.data

import android.util.Log
import dev.crazo7924.onlymusic.DownloaderImpl
import dev.crazo7924.onlymusic.MediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory

class NewPipeSearchRepository : SearchRepository {

    init {
        NewPipe.init(DownloaderImpl)
    }

    override suspend fun search(query: String): Result<List<MediaListItem>> =
        withContext(Dispatchers.IO) {
            val linkHandler = YoutubeSearchQueryHandlerFactory.getInstance()
                .fromQuery(query, MUSIC_CONTENT_FILTERS, "")

            val extractor = YoutubeMusicSearchExtractor(
                ServiceList.YouTube,
                linkHandler
            )

            Log.d(TAG, "search: About to call fetchPage()")
            val fetchPageResult = runCatching {
                extractor.fetchPage()
            }

            fetchPageResult.onFailure {
                Log.e(TAG, "search fetch error: $it")
                it.printStackTrace()
                return@withContext Result.failure(it)
            }

            val result = runCatching {
                val items = extractor.initialPage.items
                Log.d(TAG, "count: ${items.size}")
                return@runCatching items.map { item ->
                    MediaListItem(
                        title = item.name,
                        infoType = item.infoType,
                        thumbnailUri = item.thumbnails.firstOrNull()?.url,
                        mediaUri = item.url
                    )
                }
            }

            result.onFailure {
                Log.e(TAG, "search parse error: $it")
                return@withContext Result.failure(it)
            }

            result.onSuccess {
                return@withContext Result.success(it)
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
}