package app.pwhs.apexfilemanager.features.viewer.text

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TextEditorViewModel :
    BaseViewModel<TextEditorUiState, TextEditorUiAction, TextEditorUiEvent>(TextEditorUiState()) {

    override fun onAction(action: TextEditorUiAction) {
        when (action) {
            is TextEditorUiAction.LoadFile -> loadFile(action.path)
            is TextEditorUiAction.ContentChanged -> onContentChanged(action.newContent)
            is TextEditorUiAction.SaveClick -> saveFile()
            is TextEditorUiAction.ToggleSearch -> toggleSearch()
            is TextEditorUiAction.SearchQueryChanged -> onSearchQueryChanged(action.query)
            is TextEditorUiAction.ToggleWordWrap -> toggleWordWrap()
        }
    }

    private fun loadFile(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            try {
                val file = File(path)
                if (!file.exists() || !file.canRead()) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "Tệp không tồn tại hoặc không có quyền đọc"
                        )
                    }
                    return@launch
                }

                val text = withContext(Dispatchers.IO) {
                    file.readText(Charsets.UTF_8)
                }

                updateState {
                    copy(
                        filePath = path,
                        fileName = file.name,
                        content = text,
                        initialContent = text,
                        isModified = false,
                        fileSizeBytes = file.length(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Lỗi tải tệp"
                    )
                }
            }
        }
    }

    private fun onContentChanged(newContent: String) {
        val matches = calculateMatches(newContent, currentState.searchQuery)
        updateState {
            copy(
                content = newContent,
                isModified = newContent != initialContent,
                searchMatchCount = matches
            )
        }
    }

    private fun saveFile() {
        val path = currentState.filePath
        val content = currentState.content
        if (path.isEmpty()) return

        viewModelScope.launch {
            updateState { copy(isSaving = true) }
            try {
                withContext(Dispatchers.IO) {
                    val file = File(path)
                    // Tạo backup file .bak nếu file gốc tồn tại
                    if (file.exists() && file.length() > 0) {
                        val backupFile = File("${file.absolutePath}.bak")
                        file.copyTo(backupFile, overwrite = true)
                    }
                    FileOutputStream(file).use { fos ->
                        fos.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
                updateState {
                    copy(
                        initialContent = content,
                        isModified = false,
                        isSaving = false,
                        fileSizeBytes = File(path).length()
                    )
                }
                sendEvent(TextEditorUiEvent.ShowToast("Đã lưu tệp thành công"))
            } catch (e: Exception) {
                updateState { copy(isSaving = false) }
                sendEvent(TextEditorUiEvent.ShowToast("Lưu thất bại: ${e.localizedMessage}"))
            }
        }
    }

    private fun toggleSearch() {
        updateState {
            val nextVisible = !isSearchVisible
            val matches = if (nextVisible) calculateMatches(content, searchQuery) else 0
            copy(isSearchVisible = nextVisible, searchMatchCount = matches)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        val matches = calculateMatches(currentState.content, query)
        updateState {
            copy(searchQuery = query, searchMatchCount = matches)
        }
    }

    private fun toggleWordWrap() {
        updateState { copy(isWordWrap = !isWordWrap) }
    }

    private fun calculateMatches(content: String, query: String): Int {
        if (query.isEmpty() || content.isEmpty()) return 0
        return try {
            Regex.escape(query).toRegex(RegexOption.IGNORE_CASE).findAll(content).count()
        } catch (_: Exception) {
            0
        }
    }
}
