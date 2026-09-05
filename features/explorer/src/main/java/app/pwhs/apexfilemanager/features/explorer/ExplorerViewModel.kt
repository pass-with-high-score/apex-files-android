package app.pwhs.apexfilemanager.features.explorer

import android.os.Environment
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.ConflictStrategy
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CopyFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CreateFolderUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetDirectoryContentsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.MoveFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RenameFileUseCase
import app.pwhs.apexfilemanager.features.explorer.model.PathSegment
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import app.pwhs.apexfilemanager.features.explorer.model.ViewMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class ExplorerViewModel(
    private val getDirectoryContentsUseCase: GetDirectoryContentsUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val copyFilesUseCase: CopyFilesUseCase,
    private val moveFilesUseCase: MoveFilesUseCase
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
            is ExplorerUiAction.NavigateUp -> handleNavigateUp()
            is ExplorerUiAction.SearchClick -> sendEvent(ExplorerUiEvent.NavigateToSearch)

            // Selection
            is ExplorerUiAction.ToggleSelect -> toggleSelect(action.item)
            is ExplorerUiAction.SelectAll -> updateState { copy(selectedItems = files.toSet()) }
            is ExplorerUiAction.ClearSelection -> updateState { copy(selectedItems = emptySet()) }

            // CRUD
            is ExplorerUiAction.CreateFolder -> createFolder(action.name)
            is ExplorerUiAction.Rename -> renameFile(action.item, action.newName)
            is ExplorerUiAction.DeleteSelected -> deleteSelected()
            is ExplorerUiAction.DeleteSingle -> deleteSingle(action.item)
            is ExplorerUiAction.CopySelected -> startClipboard(ClipboardOperation.COPY)
            is ExplorerUiAction.MoveSelected -> startClipboard(ClipboardOperation.MOVE)
            is ExplorerUiAction.PasteClipboard -> pasteClipboard()
            is ExplorerUiAction.CancelClipboard -> updateState { copy(clipboard = null) }
        }
    }

    private fun handleNavigateUp() {
        if (currentState.isSelectionMode) {
            updateState { copy(selectedItems = emptySet()) }
            return
        }
        val crumbs = currentState.breadcrumbs
        if (crumbs.size > 1) {
            val parentPath = crumbs[crumbs.size - 2].fullPath
            loadDirectory(parentPath)
        } else {
            sendEvent(ExplorerUiEvent.NavigateBack)
        }
    }

    private fun handleFileClick(item: FileItem) {
        if (currentState.isSelectionMode) {
            toggleSelect(item)
            return
        }
        if (item.isDirectory) {
            loadDirectory(item.path)
        } else if (isArchiveFile(item.name)) {
            sendEvent(ExplorerUiEvent.OpenArchive(item.path))
        } else if (isImageFile(item.name, item.mimeType)) {
            sendEvent(ExplorerUiEvent.OpenImageViewer(item.path))
        } else if (isTextOrCodeFile(item.name, item.mimeType)) {
            sendEvent(ExplorerUiEvent.OpenTextEditor(item.path))
        } else {
            sendEvent(ExplorerUiEvent.OpenFileExternal(item.path, item.mimeType))
        }
    }

    private fun isArchiveFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "jar")
    }

    private fun isImageFile(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("image/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    }

    private fun isTextOrCodeFile(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("text/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf(
            "txt", "json", "xml", "html", "htm", "css", "js", "ts", "kt", "java",
            "py", "c", "cpp", "h", "md", "log", "properties", "gradle", "sh", "yml",
            "yaml", "ini", "conf", "env", "sql", "csv"
        )
    }

    private fun toggleSelect(item: FileItem) {
        val current = currentState.selectedItems.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        updateState { copy(selectedItems = current) }
    }

    private fun loadDirectory(path: String) {
        val targetPath = path.ifEmpty { Environment.getExternalStorageDirectory().absolutePath }
        val breadcrumbs = buildBreadcrumbs(targetPath)

        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true,
                    currentPath = targetPath,
                    breadcrumbs = breadcrumbs,
                    selectedItems = emptySet(),
                    errorMessage = null
                )
            }

            getDirectoryContentsUseCase(targetPath, currentState.showHiddenFiles)
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage, files = emptyList()) }
                }
                .collect { items ->
                    val sorted = sortFiles(items, currentState.sortOption)
                    updateState { copy(isLoading = false, files = sorted, errorMessage = null) }
                }
        }
    }

    private fun createFolder(name: String) {
        viewModelScope.launch {
            val result = createFolderUseCase(currentState.currentPath, name)
            result.onSuccess {
                sendEvent(ExplorerUiEvent.ShowToast("Đã tạo thư mục: $name"))
                loadDirectory(currentState.currentPath)
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
            }
        }
    }

    private fun renameFile(item: FileItem, newName: String) {
        viewModelScope.launch {
            val result = renameFileUseCase(item.path, newName)
            result.onSuccess {
                sendEvent(ExplorerUiEvent.ShowToast("Đã đổi tên thành: $newName"))
                loadDirectory(currentState.currentPath)
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
            }
        }
    }

    private fun deleteSelected() {
        val paths = currentState.selectedItems.map { it.path }
        deletePaths(paths)
    }

    private fun deleteSingle(item: FileItem) {
        deletePaths(listOf(item.path))
    }

    private fun deletePaths(paths: List<String>) {
        viewModelScope.launch {
            val result = deleteFilesUseCase(paths)
            result.onSuccess { count ->
                sendEvent(ExplorerUiEvent.ShowToast("Đã xóa $count mục"))
                loadDirectory(currentState.currentPath)
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi xóa: ${e.message}"))
            }
        }
    }

    private fun startClipboard(operation: ClipboardOperation) {
        val paths = currentState.selectedItems.map { it.path }
        if (paths.isEmpty()) return
        updateState {
            copy(
                clipboard = ClipboardState(operation, paths),
                selectedItems = emptySet()
            )
        }
        val opName = if (operation == ClipboardOperation.COPY) "Sao chép" else "Di chuyển"
        sendEvent(ExplorerUiEvent.ShowToast("Đã chọn ${paths.size} mục để $opName"))
    }

    private fun pasteClipboard() {
        val clip = currentState.clipboard ?: return
        val targetDir = currentState.currentPath

        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val result = if (clip.operation == ClipboardOperation.COPY) {
                copyFilesUseCase(clip.sourcePaths, targetDir, ConflictStrategy.AUTO_RENAME)
            } else {
                moveFilesUseCase(clip.sourcePaths, targetDir, ConflictStrategy.AUTO_RENAME)
            }

            result.onSuccess { count ->
                val opName = if (clip.operation == ClipboardOperation.COPY) "Sao chép" else "Di chuyển"
                sendEvent(ExplorerUiEvent.ShowToast("$opName thành công $count mục"))
                updateState { copy(clipboard = null) }
                loadDirectory(targetDir)
            }.onFailure { e ->
                updateState { copy(isLoading = false) }
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
            }
        }
    }

    private fun buildBreadcrumbs(fullPath: String): List<PathSegment> {
        val primaryRoot = Environment.getExternalStorageDirectory().absolutePath
        val segments = mutableListOf<PathSegment>()

        if (fullPath.startsWith(primaryRoot)) {
            // Thư mục gốc là Bộ nhớ trong
            segments.add(PathSegment(name = "Bộ nhớ trong", fullPath = primaryRoot))
            val subPath = fullPath.removePrefix(primaryRoot).trimStart('/')
            if (subPath.isNotEmpty()) {
                val parts = subPath.split('/')
                var accumulated = primaryRoot
                for (part in parts) {
                    accumulated += "/$part"
                    segments.add(PathSegment(name = part, fullPath = accumulated))
                }
            }
            return segments
        }

        // Với các phân vùng khác
        val file = File(fullPath)
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
        return files.sortedWith(compareByDescending<FileItem> { it.isDirectory }.then(comparator))
    }
}
