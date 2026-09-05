package app.pwhs.apexfilemanager.features.search

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory

data class SearchUiState(
    val query: String = "",
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val results: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface SearchUiAction : UiAction {
    data class UpdateQuery(val query: String) : SearchUiAction
    data class SelectCategory(val category: SearchCategory) : SearchUiAction
    data class FileClick(val item: FileItem) : SearchUiAction
    data object ClearQuery : SearchUiAction
}

sealed interface SearchUiEvent : UiEvent {
    data class OpenFile(val path: String, val mimeType: String) : SearchUiEvent
    data class OpenDirectory(val path: String) : SearchUiEvent
    data object NavigateBack : SearchUiEvent
}
