package app.pwhs.apexfilemanager.features.cleaner.analysis.components

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageAnalysis
import app.pwhs.apexfilemanager.features.cleaner.R

@Composable
fun AnalysisTab(
    analysis: StorageAnalysis?,
    modifier: Modifier = Modifier
) {
    if (analysis == null) return
    val context = LocalContext.current
    val usedFormatted = Formatter.formatFileSize(context, analysis.usedBytes)
    val totalFormatted = Formatter.formatFileSize(context, analysis.totalBytes)
    val percentage = if (analysis.totalBytes > 0) ((analysis.usedBytes.toFloat() / analysis.totalBytes) * 100).toInt() else 0

    val categories = listOf(
        stringResource(R.string.cleaner_cat_images) to analysis.imagesBytes,
        stringResource(R.string.cleaner_cat_videos) to analysis.videosBytes,
        stringResource(R.string.cleaner_cat_audio) to analysis.audioBytes,
        stringResource(R.string.cleaner_cat_documents) to analysis.documentsBytes,
        stringResource(R.string.cleaner_cat_archives) to analysis.archivesBytes,
        stringResource(R.string.cleaner_cat_apks) to analysis.apksBytes,
        stringResource(R.string.cleaner_cat_others) to analysis.otherBytes
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.cleaner_used_total, usedFormatted, totalFormatted, percentage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.cleaner_storage_distribution),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(categories) { (name, bytes) ->
            CategoryRow(name = name, bytes = bytes, totalBytes = analysis.usedBytes)
        }
    }
}

@Composable
private fun CategoryRow(name: String, bytes: Long, totalBytes: Long) {
    val context = LocalContext.current
    val formatted = Formatter.formatFileSize(context, bytes)
    val fraction = if (totalBytes > 0) (bytes.toFloat() / totalBytes) else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                Text(text = formatted, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
