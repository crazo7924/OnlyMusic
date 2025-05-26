package dev.crazo7924.onlymusic.repository

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList

class NewPipeMusicRepository : MusicRepository {
    override suspend fun loadMediaFromUri(uri: String?): Result<MediaItem> =
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


    override suspend fun loadPlaylistFromUri(uri: String?): List<Result<MediaItem>> = withContext(
        Dispatchers.IO
    ) {
        val youtubeService = ServiceList.YouTube
        val playListExtractor = youtubeService.getPlaylistExtractor(uri)

        val outcome = runCatching {  playListExtractor.fetchPage() }

        outcome.onFailure {
            return@withContext emptyList<Result<MediaItem>>()
        }

        return@withContext playListExtractor.initialPage.items.map { item ->
            loadMediaFromUri(item.url)
        }
    }
}