package app.pwhs.apexfilemanager.features.cleaner.analysis

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.JunkFile
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageAnalysis

data class CleanerUiState(
    val analysis: StorageAnalysis? = null,
    val largeFiles: List<FileItem> = emptyList(),
    val junkFiles: List<JunkFile> = emptyList(),
    val selectedJunkPaths: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isCleaning: Boolean = false,
    val selectedTab: Int = 0,
    val errorMessage: String? = null
) : UiState {
    val selectedJunkSizeBytes: Long
        get() = junkFiles.filter { it.path in selectedJunkPaths }.sumOf { it.sizeBytes }

    val totalJunkSizeBytes: Long
        get() = junkFiles.sumOf { it.sizeBytes }
}

sealed interface CleanerUiAction : UiAction {
    data object LoadData : CleanerUiAction
    data class SelectTab(val index: Int) : CleanerUiAction
    data class ToggleSelectJunk(val path: String) : CleanerUiAction
    data object SelectAllJunk : CleanerUiAction
    data object CleanSelectedJunk : CleanerUiAction
}

sealed interface CleanerUiEvent : UiEvent {
    data class ShowToast(val message: String) : CleanerUiEvent
    data object NavigateBack : CleanerUiEvent
}
