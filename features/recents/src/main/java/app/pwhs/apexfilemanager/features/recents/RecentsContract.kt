package app.pwhs.apexfilemanager.features.recents

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem

data class RecentsUiState(
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RecentsUiAction : UiAction {
    data object LoadRecents : RecentsUiAction
    data class FileClick(val item: FileItem) : RecentsUiAction
}

sealed interface RecentsUiEvent : UiEvent {
    data class OpenFileExternal(val path: String, val mimeType: String) : RecentsUiEvent
    data class OpenArchive(val path: String) : RecentsUiEvent
    data class OpenApkDetail(val path: String) : RecentsUiEvent
    data class OpenTextEditor(val path: String) : RecentsUiEvent
    data class OpenImageViewer(val path: String) : RecentsUiEvent
    data class ShowToast(val message: String) : RecentsUiEvent
    data object NavigateBack : RecentsUiEvent
}
