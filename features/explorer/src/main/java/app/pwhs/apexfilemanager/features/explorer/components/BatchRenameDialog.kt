package app.pwhs.apexfilemanager.features.explorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.core.storage.domain.usecase.BatchRenameRule
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RenamePreviewItem
import app.pwhs.apexfilemanager.features.explorer.R

private enum class RenameMode {
    PREFIX_SUFFIX,
    FIND_REPLACE,
    NUMBERING
}

@Composable
fun BatchRenameDialog(
    selectedPaths: List<String>,
    onGeneratePreview: (List<String>, BatchRenameRule) -> List<RenamePreviewItem>,
    onApplyRename: (List<RenamePreviewItem>) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(RenameMode.NUMBERING) }
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var baseName by remember { mutableStateOf("File") }
    var startIndex by remember { mutableIntStateOf(1) }

    val currentRule = remember(mode, prefix, suffix, findText, replaceText, baseName, startIndex) {
        when (mode) {
            RenameMode.PREFIX_SUFFIX -> BatchRenameRule.AddPrefixSuffix(prefix, suffix)
            RenameMode.FIND_REPLACE -> BatchRenameRule.FindAndReplace(findText, replaceText)
            RenameMode.NUMBERING -> BatchRenameRule.AutoNumbering(baseName, startIndex, 3)
        }
    }

    val previewList = remember(selectedPaths, currentRule) {
        onGeneratePreview(selectedPaths, currentRule)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.explorer_batch_rename),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mode selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == RenameMode.NUMBERING,
                        onClick = { mode = RenameMode.NUMBERING },
                        label = { Text(stringResource(R.string.explorer_batch_rename_numbering)) }
                    )
                    FilterChip(
                        selected = mode == RenameMode.PREFIX_SUFFIX,
                        onClick = { mode = RenameMode.PREFIX_SUFFIX },
                        label = { Text(stringResource(R.string.explorer_batch_rename_prefix)) }
                    )
                    FilterChip(
                        selected = mode == RenameMode.FIND_REPLACE,
                        onClick = { mode = RenameMode.FIND_REPLACE },
                        label = { Text(stringResource(R.string.explorer_batch_rename_replace)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (mode) {
                    RenameMode.NUMBERING -> {
                        OutlinedTextField(
                            value = baseName,
                            onValueChange = { baseName = it },
                            label = { Text(stringResource(R.string.explorer_batch_rename_basename)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = startIndex.toString(),
                            onValueChange = { startIndex = it.toIntOrNull() ?: 1 },
                            label = { Text("Số bắt đầu") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    RenameMode.PREFIX_SUFFIX -> {
                        OutlinedTextField(
                            value = prefix,
                            onValueChange = { prefix = it },
                            label = { Text(stringResource(R.string.explorer_batch_rename_prefix)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = suffix,
                            onValueChange = { suffix = it },
                            label = { Text(stringResource(R.string.explorer_batch_rename_suffix)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    RenameMode.FIND_REPLACE -> {
                        OutlinedTextField(
                            value = findText,
                            onValueChange = { findText = it },
                            label = { Text(stringResource(R.string.explorer_batch_rename_find)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = replaceText,
                            onValueChange = { replaceText = it },
                            label = { Text(stringResource(R.string.explorer_batch_rename_with)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.explorer_batch_rename_preview),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Preview list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(previewList) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.oldName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text(
                                text = item.newName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApplyRename(previewList) }) {
                Text(stringResource(R.string.explorer_batch_rename_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.explorer_cancel))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
