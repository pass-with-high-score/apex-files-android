package app.pwhs.apexfilemanager.features.archive

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.ArchiveEntry

data class ArchiveUiState(
    val archivePath: String = "",
    val archiveName: String = "",
    val entries: List<ArchiveEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isExtracting: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface ArchiveUiAction : UiAction {
    data class LoadArchive(val path: String, val password: String? = null) : ArchiveUiAction
    data class ExtractArchive(val destDir: String? = null, val password: String? = null) : ArchiveUiAction
}

sealed interface ArchiveUiEvent : UiEvent {
    data class ShowToast(val message: String) : ArchiveUiEvent
    data class ExtractionComplete(val destPath: String) : ArchiveUiEvent
    data object NavigateBack : ArchiveUiEvent
}
