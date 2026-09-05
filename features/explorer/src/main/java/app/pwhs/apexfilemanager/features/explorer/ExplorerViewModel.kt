package app.pwhs.apexfilemanager.features.explorer

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetDirectoryContentsUseCase
import app.pwhs.apexfilemanager.features.explorer.model.PathSegment
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class ExplorerViewModel(
    private val getDirectoryContentsUseCase: GetDirectoryContentsUseCase
) : BaseViewModel<ExplorerUiState, ExplorerUiAction, ExplorerUiEvent>(ExplorerUiState()) {

    override fun onAction(action: ExplorerUiAction) {
        when (action) {
            is ExplorerUiAction.LoadDirectory -> loadDirectory(action.path)
            is ExplorerUiAction.FileClick -> handleFileClick(action.item)
            is ExplorerUiAction.BreadcrumbClick -> loadDirectory(action.path)
            is ExplorerUiAction.ChangeViewMode -> updateState { copy(viewMode = action.mode) }
            is ExplorerUiAction.ChangeSort -> updateState {
                copy(sortOption = action.sort, files = sortFiles(files, action.sort))
            }
            is ExplorerUiAction.ToggleHiddenFiles -> {
                val newShow = !currentState.showHiddenFiles
                updateState { copy(showHiddenFiles = newShow) }
                loadDirectory(currentState.currentPath)
            }
            is ExplorerUiAction.Refresh -> loadDirectory(currentState.currentPath)
        }
    }

    private fun handleFileClick(item: FileItem) {
        if (item.isDirectory) {
            loadDirectory(item.path)
        } else {
            sendEvent(ExplorerUiEvent.OpenFileExternal(item.path, item.mimeType))
        }
    }

    private fun loadDirectory(path: String) {
        val targetPath = path.ifEmpty { "/" }
        val breadcrumbs = buildBreadcrumbs(targetPath)

        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true,
                    currentPath = targetPath,
                    breadcrumbs = breadcrumbs,
                    errorMessage = null
                )
            }

            getDirectoryContentsUseCase(targetPath, currentState.showHiddenFiles)
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { items ->
                    val sorted = sortFiles(items, currentState.sortOption)
                    updateState { copy(isLoading = false, files = sorted) }
                }
        }
    }

    private fun buildBreadcrumbs(fullPath: String): List<PathSegment> {
        val file = File(fullPath)
        val segments = mutableListOf<PathSegment>()
        var current: File? = file

        while (current != null) {
            val name = if (current.parent == null) "/" else current.name
            if (name.isNotEmpty()) {
                segments.add(0, PathSegment(name = name, fullPath = current.absolutePath))
            }
            current = current.parentFile
        }

        return segments
    }

    private fun sortFiles(files: List<FileItem>, sortOption: SortOption): List<FileItem> {
        val comparator = when (sortOption) {
            SortOption.NAME_ASC -> compareBy<FileItem> { it.name.lowercase() }
            SortOption.NAME_DESC -> compareByDescending<FileItem> { it.name.lowercase() }
            SortOption.DATE_DESC -> compareByDescending<FileItem> { it.modifiedTimestamp }
            SortOption.DATE_ASC -> compareBy<FileItem> { it.modifiedTimestamp }
            SortOption.SIZE_DESC -> compareByDescending<FileItem> { it.sizeBytes }
            SortOption.SIZE_ASC -> compareBy<FileItem> { it.sizeBytes }
        }
        // Thư mục luôn đứng trước tệp tin
        return files.sortedWith(compareByDescending<FileItem> { it.isDirectory }.then(comparator))
    }
}
