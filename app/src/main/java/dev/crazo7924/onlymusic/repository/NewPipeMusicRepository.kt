package dev.crazo7924.onlymusic.repository

import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.crazo7924.onlymusic.DownloaderImpl
import dev.crazo7924.onlymusic.MediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class NewPipeMusicRepository : MusicRepository {

    init {
        NewPipe.init(DownloaderImpl)
        YoutubeParsingHelper.setConsentAccepted(true)
    }


    override suspend fun search(query: String): List<Result<MediaListItem>> =
        withContext(Dispatchers.IO) {
            val linkHandler = YoutubeSearchQueryHandlerFactory.getInstance().fromQuery(
                query, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), ""
            )
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
                return@withContext listOf()
            }

            val result = runCatching {
                val items = extractor.initialPage.items
                Log.d(TAG, "count: ${items.size}")
                if (items.isEmpty()) return@runCatching listOf()
                return@runCatching items.map {
                    val item = it as StreamInfoItem
                    Result.success(
                        MediaListItem(
                            title = item.name,
                            artist = item.uploaderName,
                            infoType = item.infoType,
                            thumbnailUri = item.thumbnails.last().url,
                            mediaUri = item.url,
                            duration = item.duration
                        )
                    )
                }
            }

            return@withContext result.getOrElse {
                Log.e(TAG, "search failure: $it")
                return@withContext emptyList()
            }
        }

    override suspend fun loadMediaUri(uri: String?): Result<MediaItem> =
        withContext(Dispatchers.IO) {
            val youtubeService = ServiceList.YouTube
            val extractor = youtubeService.getStreamExtractor(uri)
            val mediaItemBuilder = MediaItem.Builder()
            val mediaMetadataBuilder = MediaMetadata.Builder()

            return@withContext runCatching {
                extractor.fetchPage()

                val stream = extractor.audioStreams.first()

                mediaItemBuilder.setUri(stream.content)

                mediaMetadataBuilder.setArtworkUri(
                    extractor.thumbnails.lastOrNull()?.url?.toUri()
                )
                mediaMetadataBuilder.setTitle(extractor.name)
                mediaMetadataBuilder.setArtist(extractor.uploaderName.split(" - ").first())
                mediaMetadataBuilder.setDurationMs(stream.itagItem?.approxDurationMs)
                mediaItemBuilder.setMediaMetadata(mediaMetadataBuilder.build())
                mediaItemBuilder.build()
            }
        }


    override suspend fun loadPlaylistUri(uri: String?): List<Result<MediaItem>> = withContext(
        Dispatchers.IO
    ) {
        val youtubeService = ServiceList.YouTube
        val initOutcome = runCatching { youtubeService.getPlaylistExtractor(uri) }

        val playListExtractor = initOutcome.getOrElse {
            return@withContext emptyList()
        }

        val outcome = runCatching { playListExtractor.fetchPage() }

        outcome.onFailure {
            return@withContext emptyList()
        }

        return@withContext playListExtractor.initialPage.items.map { item ->
            loadMediaUri(item.url)
        }
    }

    override suspend fun loadAutoPlaylistUri(uri: String?): List<Result<MediaItem>> =
        withContext(Dispatchers.IO) {
            if (uri == null) return@withContext emptyList()

            val id = uri.toUri().getQueryParameter("v") ?: ""
            if (id.isEmpty()) return@withContext emptyList()

            val youtubeService = ServiceList.YouTube
            val initOutcome = runCatching {
                YoutubeMixPlaylistExtractor(
                    youtubeService, YoutubePlaylistLinkHandlerFactory.getInstance().fromUrl(
                        "https://music.youtube.com/watch?v=$id&list=RD$id"
                    )
                )
            }
            val playListExtractor = initOutcome.getOrElse {
                it.printStackTrace()
                return@withContext emptyList()
            }

            val outcome = runCatching { playListExtractor.fetchPage() }
            outcome.onFailure {
                it.printStackTrace()
                return@withContext emptyList()
            }

            return@withContext playListExtractor.initialPage.items.map { item ->
                loadMediaUri(item.url)
            }
        }

    companion object {
        private const val TAG = "NewPipeMusicRepository"
    }
}