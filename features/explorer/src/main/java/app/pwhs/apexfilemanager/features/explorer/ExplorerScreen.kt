package app.pwhs.apexfilemanager.features.explorer

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.features.explorer.components.BreadcrumbBar
import app.pwhs.apexfilemanager.features.explorer.components.ClipboardBottomBar
import app.pwhs.apexfilemanager.features.explorer.components.ConfirmDeleteDialog
import app.pwhs.apexfilemanager.features.explorer.components.CreateFolderDialog
import app.pwhs.apexfilemanager.features.explorer.components.FileGridItem
import app.pwhs.apexfilemanager.features.explorer.components.FileListItem
import app.pwhs.apexfilemanager.features.explorer.components.RenameDialog
import app.pwhs.apexfilemanager.features.explorer.components.SelectionBottomBar
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode

@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        viewModel.onAction(ExplorerUiAction.NavigateUp)
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExplorerUiEvent.OpenFileExternal -> {
                    openFileWithExternalApp(context, event.path, event.mimeType)
                }
                is ExplorerUiEvent.OpenArchive -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.archive.ArchiveActivity")
                        val intent = android.content.Intent(context, clazz).apply {
                            putExtra("extra_archive_path", event.path)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở tệp nén", Toast.LENGTH_SHORT).show()
                    }
                }
                is ExplorerUiEvent.OpenApkDetail -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.appmanager.detail.ApkDetailActivity")
                        val intent = android.content.Intent(context, clazz).apply {
                            putExtra("extra_apk_path", event.path)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở tệp APK", Toast.LENGTH_SHORT).show()
                    }
                }
                is ExplorerUiEvent.OpenTextEditor -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.viewer.text.TextEditorActivity")
                        val intent = android.content.Intent(context, clazz).apply {
                            putExtra("extra_file_path", event.path)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở trình soạn thảo văn bản", Toast.LENGTH_SHORT).show()
                    }
                }
                is ExplorerUiEvent.OpenImageViewer -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.viewer.image.ImageViewerActivity")
                        val intent = android.content.Intent(context, clazz).apply {
                            putExtra("extra_image_path", event.path)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở trình xem ảnh", Toast.LENGTH_SHORT).show()
                    }
                }
                is ExplorerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ExplorerUiEvent.NavigateBack -> {
                    onBackClick()
                }
                is ExplorerUiEvent.NavigateToSearch -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.search.SearchActivity")
                        val intent = android.content.Intent(context, clazz)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Search not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    ExplorerContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerContent(
    state: ExplorerUiState,
    onAction: (ExplorerUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.explorer_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ExplorerUiAction.NavigateUp) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(ExplorerUiAction.SearchClick) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.explorer_search)
                        )
                    }

                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = stringResource(R.string.explorer_create_folder)
                        )
                    }

                    IconButton(onClick = {
                        val nextMode = if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                        onAction(ExplorerUiAction.ChangeViewMode(nextMode))
                    }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = stringResource(R.string.explorer_view_mode)
                        )
                    }

                    IconButton(onClick = { onAction(ExplorerUiAction.ToggleHiddenFiles) }) {
                        Icon(
                            imageVector = if (state.showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.explorer_hidden_files)
                        )
                    }

                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.explorer_sort)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_name_asc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.NAME_ASC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_name_desc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.NAME_DESC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_date_desc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.DATE_DESC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_date_asc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.DATE_ASC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_size_desc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.SIZE_DESC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_size_asc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.SIZE_ASC))
                                showSortMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            when {
                state.isSelectionMode -> {
                    SelectionBottomBar(
                        selectedCount = state.selectedItems.size,
                        onSelectAll = { onAction(ExplorerUiAction.SelectAll) },
                        onCopy = { onAction(ExplorerUiAction.CopySelected) },
                        onMove = { onAction(ExplorerUiAction.MoveSelected) },
                        onRename = { itemToRename = state.selectedItems.firstOrNull() },
                        onDelete = { showDeleteConfirmDialog = true },
                        onClear = { onAction(ExplorerUiAction.ClearSelection) }
                    )
                }
                state.clipboard != null -> {
                    ClipboardBottomBar(
                        clipboard = state.clipboard,
                        onPaste = { onAction(ExplorerUiAction.PasteClipboard) },
                        onCancel = { onAction(ExplorerUiAction.CancelClipboard) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.breadcrumbs.isNotEmpty()) {
                BreadcrumbBar(
                    breadcrumbs = state.breadcrumbs,
                    onSegmentClick = { path -> onAction(ExplorerUiAction.BreadcrumbClick(path)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.files.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.explorer_empty_folder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.viewMode == ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.files, key = { it.id }) { item ->
                                FileGridItem(
                                    item = item,
                                    isSelected = state.selectedItems.contains(item),
                                    isSelectionMode = state.isSelectionMode,
                                    onClick = { onAction(ExplorerUiAction.FileClick(item)) },
                                    onLongClick = { onAction(ExplorerUiAction.ToggleSelect(item)) }
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.files, key = { it.id }) { item ->
                                FileListItem(
                                    item = item,
                                    isSelected = state.selectedItems.contains(item),
                                    isSelectionMode = state.isSelectionMode,
                                    onClick = { onAction(ExplorerUiAction.FileClick(item)) },
                                    onLongClick = { onAction(ExplorerUiAction.ToggleSelect(item)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                showCreateFolderDialog = false
                onAction(ExplorerUiAction.CreateFolder(name))
            }
        )
    }

    itemToRename?.let { item ->
        RenameDialog(
            currentName = item.name,
            onDismiss = { itemToRename = null },
            onConfirm = { newName ->
                itemToRename = null
                onAction(ExplorerUiAction.Rename(item, newName))
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            itemCount = state.selectedItems.size,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                onAction(ExplorerUiAction.DeleteSelected)
            }
        )
    }
}

private fun openFileWithExternalApp(
    context: android.content.Context,
    path: String,
    mimeType: String
) {
    try {
        val file = java.io.File(path)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType.ifEmpty { "*/*" })
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.explorer_no_app_to_open),
            Toast.LENGTH_SHORT
        ).show()
    }
}
