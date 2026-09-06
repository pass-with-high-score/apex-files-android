package app.pwhs.apexfilemanager.features.appmanager.apklist

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem

data class ApkListUiState(
    val apkFiles: List<FileItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState {
    val filteredFiles: List<FileItem>
        get() {
            if (searchQuery.isBlank()) return apkFiles
            val query = searchQuery.trim().lowercase()
            return apkFiles.filter { it.name.lowercase().contains(query) || it.path.lowercase().contains(query) }
        }
}

sealed interface ApkListUiAction : UiAction {
    data object LoadApkFiles : ApkListUiAction
    data class SearchQueryChanged(val query: String) : ApkListUiAction
    data class ApkFileClick(val item: FileItem) : ApkListUiAction
}

sealed interface ApkListUiEvent : UiEvent {
    data class OpenApkDetail(val path: String) : ApkListUiEvent
    data object NavigateBack : ApkListUiEvent
}
