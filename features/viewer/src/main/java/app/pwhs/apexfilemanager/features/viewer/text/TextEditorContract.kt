package app.pwhs.apexfilemanager.features.viewer.text

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState

data class TextEditorUiState(
    val filePath: String = "",
    val fileName: String = "",
    val content: String = "",
    val initialContent: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isModified: Boolean = false,
    val searchQuery: String = "",
    val searchMatchCount: Int = 0,
    val isSearchVisible: Boolean = false,
    val isWordWrap: Boolean = false,
    val fileSizeBytes: Long = 0L,
    val errorMessage: String? = null
) : UiState {
    val lineCount: Int
        get() = if (content.isEmpty()) 1 else content.count { it == '\n' } + 1

    val charCount: Int
        get() = content.length
}

sealed interface TextEditorUiAction : UiAction {
    data class LoadFile(val path: String) : TextEditorUiAction
    data class ContentChanged(val newContent: String) : TextEditorUiAction
    data object SaveClick : TextEditorUiAction
    data object ToggleSearch : TextEditorUiAction
    data class SearchQueryChanged(val query: String) : TextEditorUiAction
    data object ToggleWordWrap : TextEditorUiAction
}

sealed interface TextEditorUiEvent : UiEvent {
    data class ShowToast(val message: String) : TextEditorUiEvent
    data object NavigateBack : TextEditorUiEvent
}
