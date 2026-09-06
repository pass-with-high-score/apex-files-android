package app.pwhs.apexfilemanager.features.explorer

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
import app.pwhs.apexfilemanager.features.explorer.components.BatchRenameDialog
import app.pwhs.apexfilemanager.features.explorer.components.BreadcrumbBar
import app.pwhs.apexfilemanager.features.explorer.components.ChecksumDialog
import app.pwhs.apexfilemanager.features.explorer.components.ClipboardBottomBar
import app.pwhs.apexfilemanager.features.explorer.components.ConfirmDeleteDialog
import app.pwhs.apexfilemanager.features.explorer.components.CreateFolderDialog
import app.pwhs.apexfilemanager.features.explorer.components.ExplorerPanesContent
import app.pwhs.apexfilemanager.features.explorer.components.ExplorerTabsOverview
import app.pwhs.apexfilemanager.features.explorer.components.RenameDialog
import app.pwhs.apexfilemanager.features.explorer.components.SelectionBottomBar
import app.pwhs.apexfilemanager.features.explorer.components.TabCounterButton
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode

@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    initialPath: String = "",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        if (state.showTabsOverview) {
            viewModel.onAction(ExplorerUiAction.ToggleTabsOverview)
        } else {
            viewModel.onAction(ExplorerUiAction.NavigateUp)
        }
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
                is ExplorerUiEvent.OpenHexViewer -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.viewer.hex.HexViewerActivity")
                        val intent = android.content.Intent(context, clazz).apply {
                            putExtra("extra_file_path", event.path)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở Trình xem Hex", Toast.LENGTH_SHORT).show()
                    }
                }
                is ExplorerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ExplorerUiEvent.NavigateBack -> onBackClick()
                is ExplorerUiEvent.NavigateToSearch -> {
                    try {
                        val clazz = Class.forName("app.pwhs.apexfilemanager.features.search.SearchActivity")
                        context.startActivity(android.content.Intent(context, clazz))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Search not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(initialPath) {
        if (state.currentPath.isEmpty()) {
            viewModel.onAction(ExplorerUiAction.LoadDirectory(initialPath))
        }
    }

    ExplorerContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        batchRenamePreview = viewModel.batchRenameUseCase::generatePreview,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerContent(
    state: ExplorerUiState,
    onAction: (ExplorerUiAction) -> Unit,
    onBackClick: () -> Unit,
    batchRenamePreview: (List<String>, app.pwhs.apexfilemanager.core.storage.domain.usecase.BatchRenameRule) -> List<app.pwhs.apexfilemanager.core.storage.domain.usecase.RenamePreviewItem>,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.explorer_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.explorer_cancel)
                        )
                    }
                },
                actions = {
                    // Safari-style Tab Counter
                    TabCounterButton(
                        count = state.tabs.size,
                        isActive = state.showTabsOverview,
                        onClick = { onAction(ExplorerUiAction.ToggleTabsOverview) }
                    )

                    // Dual Pane toggle button
                    IconButton(onClick = { onAction(ExplorerUiAction.ToggleDualPane) }) {
                        Icon(
                            imageVector = if (state.isDualPaneMode) Icons.Default.DashboardCustomize else Icons.Default.ViewColumn,
                            contentDescription = stringResource(if (state.isDualPaneMode) R.string.explorer_single_pane else R.string.explorer_dual_pane),
                            tint = if (state.isDualPaneMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { onAction(ExplorerUiAction.SearchClick) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.explorer_search)
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

                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.explorer_sort)
                        )
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.showHiddenFiles) "Ẩn tệp ẩn" else "Hiện tệp ẩn"
                                )
                            },
                            onClick = {
                                showMenu = false
                                onAction(ExplorerUiAction.ToggleHiddenFiles)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Làm mới") },
                            onClick = {
                                showMenu = false
                                onAction(ExplorerUiAction.Refresh)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!state.isSelectionMode) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.explorer_create_folder)
                    )
                }
            }
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = state.isSelectionMode) {
                    SelectionBottomBar(
                        selectedCount = state.currentActiveSelectedItems.size,
                        isDualPane = state.isDualPaneMode,
                        onSelectAll = { onAction(ExplorerUiAction.SelectAll) },
                        onCopy = { onAction(ExplorerUiAction.CopySelected) },
                        onMove = { onAction(ExplorerUiAction.MoveSelected) },
                        onRename = {
                            val item = state.currentActiveSelectedItems.firstOrNull()
                            if (item != null) showRenameDialog = item
                        },
                        onBatchRename = { onAction(ExplorerUiAction.OpenBatchRenameDialog) },
                        onCopyToOpposite = { onAction(ExplorerUiAction.CopyToOppositePane) },
                        onMoveToOpposite = { onAction(ExplorerUiAction.MoveToOppositePane) },
                        onChecksum = {
                            val item = state.currentActiveSelectedItems.firstOrNull()
                            if (item != null) onAction(ExplorerUiAction.OpenChecksumDialog(item))
                        },
                        onHexViewer = {
                            val item = state.currentActiveSelectedItems.firstOrNull()
                            if (item != null) onAction(ExplorerUiAction.OpenHexViewerAction(item))
                        },
                        onTextEditor = {
                            val item = state.currentActiveSelectedItems.firstOrNull()
                            if (item != null) onAction(ExplorerUiAction.OpenTextEditorAction(item))
                        },
                        onDelete = { showDeleteConfirmDialog = true },
                        onClear = { onAction(ExplorerUiAction.ClearSelection) }
                    )
                }

                AnimatedVisibility(visible = state.clipboard != null && !state.isSelectionMode) {
                    state.clipboard?.let { clip ->
                        ClipboardBottomBar(
                            clipboard = clip,
                            onPaste = { onAction(ExplorerUiAction.PasteClipboard) },
                            onCancel = { onAction(ExplorerUiAction.CancelClipboard) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        ExplorerPanesContent(
            state = state,
            onAction = onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    // Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                showCreateFolderDialog = false
                onAction(ExplorerUiAction.CreateFolder(name))
            }
        )
    }

    showRenameDialog?.let { item ->
        RenameDialog(
            currentName = item.name,
            onDismiss = { showRenameDialog = null },
            onConfirm = { newName ->
                showRenameDialog = null
                onAction(ExplorerUiAction.Rename(item, newName))
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            itemCount = state.currentActiveSelectedItems.size,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                onAction(ExplorerUiAction.DeleteSelected)
            }
        )
    }

    // Batch Rename Dialog
    if (state.showBatchRenameDialog) {
        val selectedPaths = state.currentActiveSelectedItems.map { it.path }
        BatchRenameDialog(
            selectedPaths = selectedPaths,
            onGeneratePreview = batchRenamePreview,
            onApplyRename = { items -> onAction(ExplorerUiAction.ApplyBatchRename(items)) },
            onDismiss = { onAction(ExplorerUiAction.DismissBatchRenameDialog) }
        )
    }

    // Checksum Dialog
    if (state.showChecksumDialog) {
        ChecksumDialog(
            fileName = state.checksumTargetItem?.name ?: "",
            checksumResult = state.checksumResult,
            isLoading = state.isCalculatingChecksum,
            onDismiss = { onAction(ExplorerUiAction.DismissChecksumDialog) }
        )
    }

    // Safari-style Multi-Tabs Overview
    if (state.showTabsOverview) {
        ExplorerTabsOverview(
            tabs = state.tabs,
            activeTabId = state.activeTabId,
            onSelectTab = { onAction(ExplorerUiAction.SelectTab(it)) },
            onCloseTab = { onAction(ExplorerUiAction.CloseTab(it)) },
            onCloseAllTabs = { onAction(ExplorerUiAction.CloseAllTabs) },
            onNewTab = { onAction(ExplorerUiAction.NewTab()) },
            onDismiss = { onAction(ExplorerUiAction.ToggleTabsOverview) }
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
