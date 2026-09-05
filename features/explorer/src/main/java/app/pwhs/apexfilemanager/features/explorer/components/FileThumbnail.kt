package app.pwhs.apexfilemanager.features.explorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import coil.compose.AsyncImage
import java.io.File

@Composable
fun FileThumbnail(
    item: FileItem,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    if (item.isDirectory) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.65f)
            )
        }
        return
    }

    val ext = item.name.substringAfterLast('.', "").lowercase()
    val isImage = item.mimeType.startsWith("image/") || ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    val isVideo = item.mimeType.startsWith("video/") || ext in setOf("mp4", "mkv", "avi", "mov", "3gp")

    if (isImage || isVideo) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(item.path),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(shape)
            )
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .size(size * 0.45f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.3f)
                    )
                }
            }
        }
        return
    }

    // Colored badge icon for specific file types
    val (icon, tintColor, bgColor) = when {
        ext == "apk" -> Triple(
            Icons.Default.Android,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2") -> Triple(
            Icons.Default.FolderZip,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer
        )
        item.mimeType.startsWith("audio/") || ext in setOf("mp3", "wav", "m4a", "flac", "ogg") -> Triple(
            Icons.Default.AudioFile,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer
        )
        ext == "pdf" || ext in setOf("doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx") -> Triple(
            Icons.Default.Description,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer
        )
        else -> Triple(
            Icons.AutoMirrored.Filled.InsertDriveFile,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
