package app.pwhs.apexfilemanager.features.vault.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import app.pwhs.apexfilemanager.features.vault.R
import app.pwhs.apexfilemanager.features.vault.main.components.VaultChangePinDialog
import app.pwhs.apexfilemanager.features.vault.main.components.VaultItemRow
import app.pwhs.apexfilemanager.features.vault.main.components.VaultSettingsDialog
import java.io.File

@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    onOpenFile: (File, String) -> Unit,
    onPickFiles: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is VaultUiEvent.OpenFile -> onOpenFile(event.file, event.mimeType)
                is VaultUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is VaultUiEvent.ShowToastRes -> Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
            }
        }
    }

    VaultContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onPickFiles = onPickFiles
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultContent(
    state: VaultUiState,
    onAction: (VaultUiAction) -> Unit,
    onNavigateBack: () -> Unit,
    onPickFiles: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.vault_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.vault_btn_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(VaultUiAction.OpenSettings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.vault_settings_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onPickFiles,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.vault_add_files)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Category Filter Chips
                CategoryFilterRow(
                    selectedCategory = state.selectedCategory,
                    onSelectCategory = { onAction(VaultUiAction.SelectCategory(it)) }
                )

                if (state.filteredItems.isEmpty()) {
                    EmptyVaultView(
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.filteredItems,
                            key = { it.id }
                        ) { item ->
                            VaultItemRow(
                                item = item,
                                onClick = { onAction(VaultUiAction.ItemClick(item)) },
                                onLongClick = { onAction(VaultUiAction.ItemLongClick(item)) },
                                onMenuClick = { onAction(VaultUiAction.ItemLongClick(item)) }
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // BottomSheet options
        state.selectedItemForMenu?.let { item ->
            VaultItemOptionsSheet(
                item = item,
                onDismiss = { onAction(VaultUiAction.DismissMenu) },
                onView = {
                    onAction(VaultUiAction.DismissMenu)
                    onAction(VaultUiAction.ItemClick(item))
                },
                onExport = {
                    onAction(VaultUiAction.ExportItem(item))
                },
                onDelete = {
                    onAction(VaultUiAction.AskDelete(item))
                }
            )
        }

        // Delete confirmation dialog
        state.itemToDelete?.let {
            AlertDialog(
                onDismissRequest = { onAction(VaultUiAction.DismissDeleteConfirm) },
                title = { Text(stringResource(R.string.vault_confirm_delete_title)) },
                text = { Text(stringResource(R.string.vault_confirm_delete_msg)) },
                confirmButton = {
                    TextButton(
                        onClick = { onAction(VaultUiAction.ExecuteDelete) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.vault_confirm_delete_btn))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(VaultUiAction.DismissDeleteConfirm) }) {
                        Text(stringResource(R.string.vault_cancel_btn))
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Settings dialog
        if (state.showSettingsDialog) {
            VaultSettingsDialog(
                isBiometricEnabled = state.isBiometricEnabled,
                onToggleBiometric = { onAction(VaultUiAction.ToggleBiometric(it)) },
                onChangePinClick = { onAction(VaultUiAction.OpenChangePin) },
                onDismiss = { onAction(VaultUiAction.DismissSettings) }
            )
        }

        // Change PIN dialog
        if (state.showChangePinDialog) {
            VaultChangePinDialog(
                onChangePin = { oldPin, newPin -> onAction(VaultUiAction.ChangePin(oldPin, newPin)) },
                onDismiss = { onAction(VaultUiAction.DismissChangePin) }
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: VaultCategory,
    onSelectCategory: (VaultCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(VaultCategory.entries) { category ->
            val label = when (category) {
                VaultCategory.ALL -> stringResource(R.string.vault_category_all)
                VaultCategory.IMAGES -> stringResource(R.string.vault_category_images)
                VaultCategory.VIDEOS -> stringResource(R.string.vault_category_videos)
                VaultCategory.DOCUMENTS -> stringResource(R.string.vault_category_docs)
                VaultCategory.OTHERS -> stringResource(R.string.vault_category_others)
            }
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun EmptyVaultView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.vault_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.vault_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultItemOptionsSheet(
    item: VaultItem,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = item.originalName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            SheetActionItem(
                icon = Icons.Default.Visibility,
                title = stringResource(R.string.vault_option_view),
                onClick = onView
            )

            SheetActionItem(
                icon = Icons.Default.FileDownload,
                title = stringResource(R.string.vault_option_export),
                onClick = onExport
            )

            SheetActionItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.vault_option_delete),
                textColor = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
