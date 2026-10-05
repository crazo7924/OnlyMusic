/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.crazo7924.onlymusic.core.MediaListItem
import dev.crazo7924.onlymusic.core.R
import org.schabi.newpipe.extractor.InfoItem

@Suppress("FunctionNaming")
@Composable
fun MediaListItemRow(
    mediaListItem: MediaListItem,
    modifier: Modifier = Modifier,
    imageSize: Dp = 64.dp,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    MediaListItemRow(
        title = mediaListItem.title,
        artist = mediaListItem.artist,
        thumbnailUri = mediaListItem.thumbnailUri,
        infoType = mediaListItem.infoType,
        modifier = modifier,
        imageSize = imageSize,
        trailingContent = trailingContent
    )
}

@Suppress("FunctionNaming")
@Composable
fun MediaListItemRow(
    title: String?,
    artist: String?,
    thumbnailUri: Any?,
    infoType: InfoItem.InfoType,
    modifier: Modifier = Modifier,
    imageSize: Dp = 64.dp,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val displayTitle = title ?: stringResource(R.string.song_unknown_title)
    val displayArtist = artist ?: stringResource(R.string.song_unknown_artist)

    val intrinsicSize = with(LocalDensity.current) {
        Size(48.dp.toPx(), 48.dp.toPx())
    }
    val icon = iconForInfoType(infoType, intrinsicSize)

    CommonListItem(
        title = displayTitle,
        artist = displayArtist,
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(imageSize),
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .data(thumbnailUri)
                    .build(),
                contentDescription = null,
                error = icon,
                placeholder = icon,
                fallback = icon,
                contentScale = ContentScale.Crop,
                clipToBounds = true
            )
        },
        modifier = modifier,
        trailingContent = trailingContent
    )
}
