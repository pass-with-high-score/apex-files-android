package app.pwhs.apexfilemanager.features.explorer

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.features.explorer.model.PathSegment
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode

data class ExplorerUiState(
    val currentPath: String = "",
    val breadcrumbs: List<PathSegment> = emptyList(),
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val showHiddenFiles: Boolean = false
) : UiState

sealed interface ExplorerUiAction : UiAction {
    data class LoadDirectory(val path: String) : ExplorerUiAction
    data class FileClick(val item: FileItem) : ExplorerUiAction
    data class BreadcrumbClick(val path: String) : ExplorerUiAction
    data class ChangeViewMode(val mode: ViewMode) : ExplorerUiAction
    data class ChangeSort(val sort: SortOption) : ExplorerUiAction
    data object ToggleHiddenFiles : ExplorerUiAction
    data object Refresh : ExplorerUiAction
}

sealed interface ExplorerUiEvent : UiEvent {
    data class OpenFileExternal(val path: String, val mimeType: String) : ExplorerUiEvent
    data class ShowToast(val message: String) : ExplorerUiEvent
}
