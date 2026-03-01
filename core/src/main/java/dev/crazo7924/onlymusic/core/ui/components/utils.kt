package dev.crazo7924.onlymusic.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.schabi.newpipe.extractor.InfoItem

@Composable
fun iconForInfoType(infoType: InfoItem.InfoType, intrinsicSize: Size? = null): Painter {
    return forwardingPainter(
        rememberVectorPainter(
            when (infoType) {
                // Song
                InfoItem.InfoType.STREAM -> Icons.Rounded.MusicNote
                // Album / Playlist
                InfoItem.InfoType.PLAYLIST -> Icons.AutoMirrored.Rounded.List

                // Artist or Channel
                InfoItem.InfoType.CHANNEL -> Icons.Rounded.Person

                // Comment (unused in here but kept for exhaustive branching)
                InfoItem.InfoType.COMMENT -> Icons.AutoMirrored.Rounded.Comment
            }
        ),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
        intrinsicSize = intrinsicSize,
    )
}