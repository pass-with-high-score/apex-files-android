package app.pwhs.apexfilemanager.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.R
import app.pwhs.apexfilemanager.features.home.HomeCategory

private data class CategoryUiModel(
    val category: HomeCategory,
    val titleRes: Int,
    val icon: ImageVector,
    val iconContainerColor: @Composable () -> Color,
    val iconTint: @Composable () -> Color
)

/**
 * Lưới 4 cột phân loại tệp tin phổ biến (Downloads, Ảnh, Video, Âm thanh, Tài liệu, Tệp nén, APK, Gần đây).
 */
@Composable
fun MediaCategoriesGrid(
    onCategoryClick: (HomeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryUiModel(
            category = HomeCategory.DOWNLOADS,
            titleRes = R.string.home_category_downloads,
            icon = Icons.Default.Download,
            iconContainerColor = { MaterialTheme.colorScheme.primaryContainer },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.IMAGES,
            titleRes = R.string.home_category_images,
            icon = Icons.Default.Image,
            iconContainerColor = { MaterialTheme.colorScheme.secondaryContainer },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.VIDEOS,
            titleRes = R.string.home_category_videos,
            icon = Icons.Default.Movie,
            iconContainerColor = { MaterialTheme.colorScheme.tertiaryContainer },
            iconTint = { MaterialTheme.colorScheme.onTertiaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.AUDIO,
            titleRes = R.string.home_category_audio,
            icon = Icons.Default.Audiotrack,
            iconContainerColor = { MaterialTheme.colorScheme.primaryContainer },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.DOCUMENTS,
            titleRes = R.string.home_category_documents,
            icon = Icons.Default.Description,
            iconContainerColor = { MaterialTheme.colorScheme.secondaryContainer },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.ARCHIVES,
            titleRes = R.string.home_category_archives,
            icon = Icons.Default.FolderZip,
            iconContainerColor = { MaterialTheme.colorScheme.tertiaryContainer },
            iconTint = { MaterialTheme.colorScheme.onTertiaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.APKS,
            titleRes = R.string.home_category_apks,
            icon = Icons.Default.Android,
            iconContainerColor = { MaterialTheme.colorScheme.primaryContainer },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer }
        ),
        CategoryUiModel(
            category = HomeCategory.RECENTS,
            titleRes = R.string.home_category_recents,
            icon = Icons.Default.Schedule,
            iconContainerColor = { MaterialTheme.colorScheme.secondaryContainer },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer }
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val rows = categories.chunked(4)
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (item in row) {
                    CategoryItemView(
                        item = item,
                        onClick = { onCategoryClick(item.category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItemView(
    item: CategoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    color = item.iconContainerColor(),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.titleRes),
                tint = item.iconTint(),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(item.titleRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
