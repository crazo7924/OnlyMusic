/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

import android.util.Log
import androidx.core.net.toUri
import dev.crazo7924.onlymusic.core.DownloaderImpl
import dev.crazo7924.onlymusic.core.MediaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class NewPipeMusicRepository : MusicRepository {

    init {
        NewPipe.init(DownloaderImpl)
        YoutubeParsingHelper.setConsentAccepted(true)
    }


    override suspend fun search(query: String): Flow<Result<MediaListItem>> =
        flow {
            val linkHandler = YoutubeSearchQueryHandlerFactory.getInstance().fromQuery(
                query, listOf(MUSIC_SONGS), ""
            )
            val extractor = YoutubeMusicSearchExtractor(
                ServiceList.YouTube, linkHandler
            )

            Log.d(TAG, "search: About to call fetchPage()")
            val fetchPageResult = runCatching {
                extractor.fetchPage()
            }

            fetchPageResult.onFailure {
                Log.e(TAG, "search fetch error: $it")
            }

            val result = runCatching {
                val items = extractor.initialPage.items
                Log.d(TAG, "search results count: ${items.size}")
                if (items.isEmpty()) Log.e(TAG, "No results")
                return@runCatching items.map { item ->
                    if (item !is StreamInfoItem) emit(Result.failure(Throwable("Got an unexpected InfoItem type in search results")))
                    else emit(
                        Result.success(
                            MediaListItem(
                                id = item.url.substringAfter("?v="),
                                title = item.name,
                                artist = item.uploaderName.substringBefore(" - "),
                                infoType = item.infoType,
                                thumbnailUri = item.thumbnails.maxBy { image -> image.height }.url,
                                mediaUri = item.url, // not actual media uri so that search results show up quickly
                                duration = item.duration
                            )
                        )
                    )
                }
            }

            result.getOrElse {
                Log.e(TAG, "search failure: $it")
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun loadMediaUri(uri: String?): Result<MediaListItem> =
        withContext(Dispatchers.IO) {
            val youtubeService = ServiceList.YouTube
            val extractor = youtubeService.getStreamExtractor(uri)

            val result = runCatching {
                extractor.fetchPage()
            }

            return@withContext result.fold(
                onSuccess = {
                    Log.d(
                        TAG,
                        "loadMediaUri: Successfully extracted ${extractor.url}. Now parsing..."
                    )
                    val mediaUri = (extractor.audioStreams.maxByOrNull { it.bitrate } ?: extractor.videoStreams.maxByOrNull { it.bitrate })?.content
                    if (mediaUri == null) return@fold Result.failure(Exception("No playable streams found"))
                    Result.success(
                        MediaListItem(
                            id = extractor.url.substringAfter("?v="),
                            title = extractor.name,
                            artist = extractor.uploaderName.substringBefore(" - "),
                            infoType = InfoType.STREAM,
                            thumbnailUri = extractor.thumbnails.maxBy { image -> image.height }.url,
                            mediaUri = mediaUri,
                            duration = extractor.length * 1000L
                        )
                    )
                },
                onFailure = {
                    Result.failure(it)
                }
            )
        }


    override suspend fun loadPlaylistUri(uri: String?): Flow<Result<MediaListItem>> = flow {
        val youtubeService = ServiceList.YouTube
        val initOutcome = runCatching { youtubeService.getPlaylistExtractor(uri) }
        val playListExtractor = initOutcome.getOrElse {
            emit(Result.failure(it))
            return@flow
        }

        val outcome = runCatching { playListExtractor.fetchPage() }

        outcome.onSuccess {

            playListExtractor?.initialPage?.items?.forEach { item ->
                emit(loadMediaUri(item.url))
            }
        }

    }.flowOn(Dispatchers.IO)

    override suspend fun loadAutoPlaylistUri(uri: String?): Flow<Result<MediaListItem>> =
        flow {
            if (uri == null) return@flow

            val id = uri.toUri().getQueryParameter("v") ?: ""
            if (id.isEmpty()) return@flow

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
                return@flow
            }

            val outcome = runCatching { playListExtractor.fetchPage() }
            outcome.onFailure {
                it.printStackTrace()
                return@flow
            }

            playListExtractor.initialPage.items.forEach { item ->
                emit(loadMediaUri(item.url))
            }
        }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "NewPipeMusicRepository"
    }
}