package app.pwhs.apexfilemanager.features.explorer

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ChecksumResult
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RenamePreviewItem
import app.pwhs.apexfilemanager.features.explorer.model.ExplorerTab
import app.pwhs.apexfilemanager.features.explorer.model.PathSegment
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode

enum class ClipboardOperation {
    COPY,
    MOVE
}

enum class ActivePane {
    PRIMARY,
    SECONDARY
}

data class ClipboardState(
    val operation: ClipboardOperation,
    val sourcePaths: List<String>
)

data class ExplorerUiState(
    val currentPath: String = "",
    val breadcrumbs: List<PathSegment> = emptyList(),
    val files: List<FileItem> = emptyList(),
    val selectedItems: Set<FileItem> = emptySet(),
    val clipboard: ClipboardState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val showHiddenFiles: Boolean = false,

    // Dual pane states
    val isDualPaneMode: Boolean = false,
    val activePane: ActivePane = ActivePane.PRIMARY,
    val secondaryPath: String = "",
    val secondaryBreadcrumbs: List<PathSegment> = emptyList(),
    val secondaryFiles: List<FileItem> = emptyList(),
    val secondarySelectedItems: Set<FileItem> = emptySet(),
    val secondaryIsLoading: Boolean = false,

    // Power user dialog states
    val showBatchRenameDialog: Boolean = false,
    val showChecksumDialog: Boolean = false,
    val checksumTargetItem: FileItem? = null,
    val checksumResult: ChecksumResult? = null,
    val isCalculatingChecksum: Boolean = false,

    // Safari-style Multi-Tabs
    val tabs: List<ExplorerTab> = emptyList(),
    val activeTabId: String = "",
    val showTabsOverview: Boolean = false
) : UiState {
    val isSelectionMode: Boolean
        get() = (if (activePane == ActivePane.PRIMARY) selectedItems else secondarySelectedItems).isNotEmpty()

    val currentActiveSelectedItems: Set<FileItem>
        get() = if (activePane == ActivePane.PRIMARY) selectedItems else secondarySelectedItems
}

sealed interface ExplorerUiAction : UiAction {
    data class LoadDirectory(val path: String) : ExplorerUiAction
    data class FileClick(val item: FileItem) : ExplorerUiAction
    data class BreadcrumbClick(val path: String) : ExplorerUiAction
    data class ChangeViewMode(val mode: ViewMode) : ExplorerUiAction
    data class ChangeSort(val sort: SortOption) : ExplorerUiAction
    data object ToggleHiddenFiles : ExplorerUiAction
    data object Refresh : ExplorerUiAction
    data object NavigateUp : ExplorerUiAction
    data object SearchClick : ExplorerUiAction

    // Thao tác chọn nhiều
    data class ToggleSelect(val item: FileItem) : ExplorerUiAction
    data object SelectAll : ExplorerUiAction
    data object ClearSelection : ExplorerUiAction

    // Thao tác CRUD
    data class CreateFolder(val name: String) : ExplorerUiAction
    data class Rename(val item: FileItem, val newName: String) : ExplorerUiAction
    data object DeleteSelected : ExplorerUiAction
    data class DeleteSingle(val item: FileItem) : ExplorerUiAction
    data object CopySelected : ExplorerUiAction
    data object MoveSelected : ExplorerUiAction
    data object PasteClipboard : ExplorerUiAction
    data object CancelClipboard : ExplorerUiAction

    // Dual Pane
    data object ToggleDualPane : ExplorerUiAction
    data class SwitchActivePane(val pane: ActivePane) : ExplorerUiAction
    data object CopyToOppositePane : ExplorerUiAction
    data object MoveToOppositePane : ExplorerUiAction

    // Power Tools
    data object OpenBatchRenameDialog : ExplorerUiAction
    data object DismissBatchRenameDialog : ExplorerUiAction
    data class ApplyBatchRename(val items: List<RenamePreviewItem>) : ExplorerUiAction
    data class OpenChecksumDialog(val item: FileItem) : ExplorerUiAction
    data object DismissChecksumDialog : ExplorerUiAction
    data class OpenHexViewerAction(val item: FileItem) : ExplorerUiAction
    data class OpenTextEditorAction(val item: FileItem) : ExplorerUiAction

    // Safari-style Multi-Tabs
    data object ToggleTabsOverview : ExplorerUiAction
    data class SelectTab(val tabId: String) : ExplorerUiAction
    data class NewTab(val path: String? = null) : ExplorerUiAction
    data class CloseTab(val tabId: String) : ExplorerUiAction
    data object CloseAllTabs : ExplorerUiAction
}

sealed interface ExplorerUiEvent : UiEvent {
    data class OpenFileExternal(val path: String, val mimeType: String) : ExplorerUiEvent
    data class OpenArchive(val path: String) : ExplorerUiEvent
    data class OpenApkDetail(val path: String) : ExplorerUiEvent
    data class OpenTextEditor(val path: String) : ExplorerUiEvent
    data class OpenImageViewer(val path: String) : ExplorerUiEvent
    data class OpenHexViewer(val path: String) : ExplorerUiEvent
    data class ShowToast(val message: String) : ExplorerUiEvent
    data object NavigateBack : ExplorerUiEvent
    data object NavigateToSearch : ExplorerUiEvent
}
