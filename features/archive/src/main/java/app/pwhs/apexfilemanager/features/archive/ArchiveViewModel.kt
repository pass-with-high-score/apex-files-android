package app.pwhs.apexfilemanager.features.archive

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ExtractArchiveUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ListArchiveEntriesUseCase
import kotlinx.coroutines.launch
import java.io.File

class ArchiveViewModel(
    private val listArchiveEntriesUseCase: ListArchiveEntriesUseCase,
    private val extractArchiveUseCase: ExtractArchiveUseCase
) : BaseViewModel<ArchiveUiState, ArchiveUiAction, ArchiveUiEvent>(ArchiveUiState()) {

    override fun onAction(action: ArchiveUiAction) {
        when (action) {
            is ArchiveUiAction.LoadArchive -> loadArchive(action.path, action.password)
            is ArchiveUiAction.ExtractArchive -> extractArchive(action.destDir, action.password)
        }
    }

    private fun loadArchive(path: String, password: String?) {
        val file = File(path)
        updateState {
            copy(
                archivePath = path,
                archiveName = file.name,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = listArchiveEntriesUseCase(path, password)
            result.onSuccess { entries ->
                updateState { copy(isLoading = false, entries = entries, errorMessage = null) }
            }.onFailure { e ->
                updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                sendEvent(ArchiveUiEvent.ShowToast("Lỗi đọc tệp nén: ${e.message}"))
            }
        }
    }

    private fun extractArchive(destDir: String?, password: String?) {
        val path = currentState.archivePath
        if (path.isEmpty()) return

        val targetDir = destDir ?: run {
            val file = File(path)
            val parent = file.parent ?: file.absolutePath
            val nameWithoutExt = file.nameWithoutExtension
            "$parent/$nameWithoutExt"
        }

        viewModelScope.launch {
            updateState { copy(isExtracting = true) }
            val result = extractArchiveUseCase(path, targetDir, password)
            result.onSuccess { count ->
                updateState { copy(isExtracting = false) }
                sendEvent(ArchiveUiEvent.ShowToast("Đã giải nén thành công $count mục"))
                sendEvent(ArchiveUiEvent.ExtractionComplete(targetDir))
            }.onFailure { e ->
                updateState { copy(isExtracting = false) }
                sendEvent(ArchiveUiEvent.ShowToast("Lỗi giải nén: ${e.message}"))
            }
        }
    }
}
