package app.pwhs.apexfilemanager.features.explorer

import android.os.Environment
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.ConflictStrategy
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.usecase.BatchRenameUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CalculateChecksumUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CopyFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CreateFolderUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetDirectoryContentsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.MoveFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RenameFileUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RenamePreviewItem
import app.pwhs.apexfilemanager.features.explorer.model.PathSegment
import app.pwhs.apexfilemanager.features.explorer.model.SortOption
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class ExplorerViewModel(
    private val getDirectoryContentsUseCase: GetDirectoryContentsUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val copyFilesUseCase: CopyFilesUseCase,
    private val moveFilesUseCase: MoveFilesUseCase,
    val batchRenameUseCase: BatchRenameUseCase,
    private val calculateChecksumUseCase: CalculateChecksumUseCase
) : BaseViewModel<ExplorerUiState, ExplorerUiAction, ExplorerUiEvent>(ExplorerUiState()) {

    override fun onAction(action: ExplorerUiAction) {
        when (action) {
            is ExplorerUiAction.LoadDirectory -> loadDirectory(action.path)
            is ExplorerUiAction.FileClick -> handleFileClick(action.item)
            is ExplorerUiAction.BreadcrumbClick -> loadDirectory(action.path)
            is ExplorerUiAction.ChangeViewMode -> updateState { copy(viewMode = action.mode) }
            is ExplorerUiAction.ChangeSort -> handleSortChange(action.sort)
            is ExplorerUiAction.ToggleHiddenFiles -> handleToggleHidden()
            is ExplorerUiAction.Refresh -> refreshCurrent()
            is ExplorerUiAction.NavigateUp -> handleNavigateUp()
            is ExplorerUiAction.SearchClick -> sendEvent(ExplorerUiEvent.NavigateToSearch)

            // Selection
            is ExplorerUiAction.ToggleSelect -> toggleSelect(action.item)
            is ExplorerUiAction.SelectAll -> handleSelectAll()
            is ExplorerUiAction.ClearSelection -> handleClearSelection()

            // CRUD
            is ExplorerUiAction.CreateFolder -> createFolder(action.name)
            is ExplorerUiAction.Rename -> renameFile(action.item, action.newName)
            is ExplorerUiAction.DeleteSelected -> deleteSelected()
            is ExplorerUiAction.DeleteSingle -> deleteSingle(action.item)
            is ExplorerUiAction.CopySelected -> startClipboard(ClipboardOperation.COPY)
            is ExplorerUiAction.MoveSelected -> startClipboard(ClipboardOperation.MOVE)
            is ExplorerUiAction.PasteClipboard -> pasteClipboard()
            is ExplorerUiAction.CancelClipboard -> updateState { copy(clipboard = null) }

            // Dual Pane
            is ExplorerUiAction.ToggleDualPane -> toggleDualPane()
            is ExplorerUiAction.SwitchActivePane -> updateState { copy(activePane = action.pane) }
            is ExplorerUiAction.CopyToOppositePane -> transferToOppositePane(isMove = false)
            is ExplorerUiAction.MoveToOppositePane -> transferToOppositePane(isMove = true)

            // Power Tools
            is ExplorerUiAction.OpenBatchRenameDialog -> updateState { copy(showBatchRenameDialog = true) }
            is ExplorerUiAction.DismissBatchRenameDialog -> updateState { copy(showBatchRenameDialog = false) }
            is ExplorerUiAction.ApplyBatchRename -> executeBatchRename(action.items)
            is ExplorerUiAction.OpenChecksumDialog -> calculateChecksum(action.item)
            is ExplorerUiAction.DismissChecksumDialog -> updateState { copy(showChecksumDialog = false, checksumTargetItem = null, checksumResult = null) }
            is ExplorerUiAction.OpenHexViewerAction -> sendEvent(ExplorerUiEvent.OpenHexViewer(action.item.path))
            is ExplorerUiAction.OpenTextEditorAction -> sendEvent(ExplorerUiEvent.OpenTextEditor(action.item.path))
        }
    }

    private fun handleNavigateUp() {
        if (currentState.isSelectionMode) {
            handleClearSelection()
            return
        }
        val isSec = currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY
        val crumbs = if (isSec) currentState.secondaryBreadcrumbs else currentState.breadcrumbs
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
        } else if (isApkFile(item.name)) {
            sendEvent(ExplorerUiEvent.OpenApkDetail(item.path))
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

    private fun isApkFile(name: String): Boolean = name.endsWith(".apk", ignoreCase = true)

    private fun isArchiveFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "jar")
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
        val isSec = currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY
        if (isSec) {
            val current = currentState.secondarySelectedItems.toMutableSet()
            if (current.contains(item)) current.remove(item) else current.add(item)
            updateState { copy(secondarySelectedItems = current) }
        } else {
            val current = currentState.selectedItems.toMutableSet()
            if (current.contains(item)) current.remove(item) else current.add(item)
            updateState { copy(selectedItems = current) }
        }
    }

    private fun handleSelectAll() {
        val isSec = currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY
        if (isSec) {
            updateState { copy(secondarySelectedItems = secondaryFiles.toSet()) }
        } else {
            updateState { copy(selectedItems = files.toSet()) }
        }
    }

    private fun handleClearSelection() {
        updateState { copy(selectedItems = emptySet(), secondarySelectedItems = emptySet()) }
    }

    private fun toggleDualPane() {
        val next = !currentState.isDualPaneMode
        updateState { copy(isDualPaneMode = next) }
        if (next && currentState.secondaryPath.isEmpty()) {
            val defaultSecondary = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            loadSecondaryDirectory(defaultSecondary)
        }
    }

    fun loadDirectory(path: String) {
        val isSec = currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY
        if (isSec) {
            loadSecondaryDirectory(path)
        } else {
            loadPrimaryDirectory(path)
        }
    }

    private fun loadPrimaryDirectory(path: String) {
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

    private fun loadSecondaryDirectory(path: String) {
        val targetPath = path.ifEmpty { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath }
        val breadcrumbs = buildBreadcrumbs(targetPath)

        viewModelScope.launch {
            updateState {
                copy(
                    secondaryIsLoading = true,
                    secondaryPath = targetPath,
                    secondaryBreadcrumbs = breadcrumbs,
                    secondarySelectedItems = emptySet()
                )
            }

            getDirectoryContentsUseCase(targetPath, currentState.showHiddenFiles)
                .catch {
                    updateState { copy(secondaryIsLoading = false, secondaryFiles = emptyList()) }
                }
                .collect { items ->
                    val sorted = sortFiles(items, currentState.sortOption)
                    updateState { copy(secondaryIsLoading = false, secondaryFiles = sorted) }
                }
        }
    }

    private fun transferToOppositePane(isMove: Boolean) {
        val isFromPrimary = currentState.activePane == ActivePane.PRIMARY
        val sources = (if (isFromPrimary) currentState.selectedItems else currentState.secondarySelectedItems).map { it.path }
        val targetDir = if (isFromPrimary) currentState.secondaryPath else currentState.currentPath

        if (sources.isEmpty() || targetDir.isEmpty()) return

        viewModelScope.launch {
            updateState { copy(isLoading = true, secondaryIsLoading = true) }
            val result = if (isMove) {
                moveFilesUseCase(sources, targetDir, ConflictStrategy.AUTO_RENAME)
            } else {
                copyFilesUseCase(sources, targetDir, ConflictStrategy.AUTO_RENAME)
            }

            result.onSuccess { count ->
                val opName = if (isMove) "Đã di chuyển" else "Đã sao chép"
                sendEvent(ExplorerUiEvent.ShowToast("$opName $count mục sang bảng đối diện"))
                loadPrimaryDirectory(currentState.currentPath)
                loadSecondaryDirectory(currentState.secondaryPath)
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
                updateState { copy(isLoading = false, secondaryIsLoading = false) }
            }
        }
    }

    private fun executeBatchRename(items: List<RenamePreviewItem>) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, showBatchRenameDialog = false) }
            val result = batchRenameUseCase.executeRename(items)
            updateState { copy(isLoading = false) }
            result.onSuccess { count ->
                sendEvent(ExplorerUiEvent.ShowToast("Đã đổi tên $count tệp thành công"))
                refreshCurrent()
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi đổi tên: ${e.message}"))
            }
        }
    }

    private fun calculateChecksum(item: FileItem) {
        viewModelScope.launch {
            updateState {
                copy(
                    showChecksumDialog = true,
                    checksumTargetItem = item,
                    checksumResult = null,
                    isCalculatingChecksum = true
                )
            }
            val result = calculateChecksumUseCase(item.path)
            result.onSuccess { checksum ->
                updateState { copy(checksumResult = checksum, isCalculatingChecksum = false) }
            }.onFailure { e ->
                updateState { copy(isCalculatingChecksum = false) }
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi tính mã: ${e.message}"))
            }
        }
    }

    private fun createFolder(name: String) {
        val targetDir = if (currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY) currentState.secondaryPath else currentState.currentPath
        viewModelScope.launch {
            val result = createFolderUseCase(targetDir, name)
            result.onSuccess {
                sendEvent(ExplorerUiEvent.ShowToast("Đã tạo thư mục: $name"))
                refreshCurrent()
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
                refreshCurrent()
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
            }
        }
    }

    private fun deleteSelected() {
        val paths = currentState.currentActiveSelectedItems.map { it.path }
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
                refreshCurrent()
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi xóa: ${e.message}"))
            }
        }
    }

    private fun startClipboard(operation: ClipboardOperation) {
        val paths = currentState.currentActiveSelectedItems.map { it.path }
        if (paths.isEmpty()) return
        updateState {
            copy(
                clipboard = ClipboardState(operation, paths),
                selectedItems = emptySet(),
                secondarySelectedItems = emptySet()
            )
        }
        val opName = if (operation == ClipboardOperation.COPY) "Sao chép" else "Di chuyển"
        sendEvent(ExplorerUiEvent.ShowToast("Đã chọn ${paths.size} mục để $opName"))
    }

    private fun pasteClipboard() {
        val clip = currentState.clipboard ?: return
        val targetDir = if (currentState.isDualPaneMode && currentState.activePane == ActivePane.SECONDARY) currentState.secondaryPath else currentState.currentPath

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
                refreshCurrent()
            }.onFailure { e ->
                sendEvent(ExplorerUiEvent.ShowToast("Lỗi: ${e.message}"))
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun handleSortChange(sort: SortOption) {
        updateState {
            copy(
                sortOption = sort,
                files = sortFiles(files, sort),
                secondaryFiles = sortFiles(secondaryFiles, sort)
            )
        }
    }

    private fun handleToggleHidden() {
        val next = !currentState.showHiddenFiles
        updateState { copy(showHiddenFiles = next) }
        refreshCurrent()
    }

    private fun refreshCurrent() {
        loadPrimaryDirectory(currentState.currentPath)
        if (currentState.isDualPaneMode && currentState.secondaryPath.isNotEmpty()) {
            loadSecondaryDirectory(currentState.secondaryPath)
        }
    }

    private fun sortFiles(items: List<FileItem>, sort: SortOption): List<FileItem> {
        val folders = items.filter { it.isDirectory }
        val files = items.filter { !it.isDirectory }

        val comparator: Comparator<FileItem> = when (sort) {
            SortOption.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortOption.NAME_DESC -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortOption.DATE_DESC -> compareByDescending { it.modifiedTimestamp }
            SortOption.DATE_ASC -> compareBy { it.modifiedTimestamp }
            SortOption.SIZE_DESC -> compareByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> compareBy { it.sizeBytes }
        }

        return folders.sortedWith(comparator) + files.sortedWith(comparator)
    }

    private fun buildBreadcrumbs(fullPath: String): List<PathSegment> {
        val segments = mutableListOf<PathSegment>()
        var current = File(fullPath)
        val pathList = mutableListOf<File>()

        while (current.parentFile != null) {
            pathList.add(0, current)
            current = current.parentFile ?: break
        }
        pathList.add(0, current)

        for (f in pathList) {
            val displayName = if (f.absolutePath == "/") "Gốc"
            else if (f.absolutePath == Environment.getExternalStorageDirectory().absolutePath) "Bộ nhớ"
            else f.name

            segments.add(PathSegment(name = displayName, fullPath = f.absolutePath))
        }

        return segments
    }
}
