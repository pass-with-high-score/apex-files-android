package app.pwhs.apexfilemanager.features.recents

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetRecentFilesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class RecentsViewModel(
    private val getRecentFilesUseCase: GetRecentFilesUseCase
) : BaseViewModel<RecentsUiState, RecentsUiAction, RecentsUiEvent>(RecentsUiState()) {

    init {
        onAction(RecentsUiAction.LoadRecents)
    }

    override fun onAction(action: RecentsUiAction) {
        when (action) {
            is RecentsUiAction.LoadRecents -> loadRecents()
            is RecentsUiAction.FileClick -> handleFileClick(action.item)
        }
    }

    private fun loadRecents() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            getRecentFilesUseCase(50)
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { files ->
                    updateState { copy(isLoading = false, files = files, errorMessage = null) }
                }
        }
    }

    private fun handleFileClick(item: app.pwhs.apexfilemanager.core.storage.domain.model.FileItem) {
        val ext = item.name.substringAfterLast('.', "").lowercase()
        if (ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")) {
            sendEvent(RecentsUiEvent.OpenArchive(item.path))
        } else {
            sendEvent(RecentsUiEvent.OpenFileExternal(item.path, item.mimeType))
        }
    }
}
