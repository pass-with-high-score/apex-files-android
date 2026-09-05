package app.pwhs.apexfilemanager.features.search

import android.os.Environment
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory
import app.pwhs.apexfilemanager.core.storage.domain.usecase.SearchFilesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchFilesUseCase: SearchFilesUseCase
) : BaseViewModel<SearchUiState, SearchUiAction, SearchUiEvent>(SearchUiState()) {

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    executeSearch(query, currentState.selectedCategory)
                }
        }
    }

    override fun onAction(action: SearchUiAction) {
        when (action) {
            is SearchUiAction.UpdateQuery -> {
                updateState { copy(query = action.query) }
                queryFlow.value = action.query
            }
            is SearchUiAction.SelectCategory -> {
                updateState { copy(selectedCategory = action.category) }
                executeSearch(currentState.query, action.category)
            }
            is SearchUiAction.ClearQuery -> {
                updateState { copy(query = "", results = emptyList(), isSearching = false) }
                queryFlow.value = ""
                searchJob?.cancel()
            }
            is SearchUiAction.FileClick -> {
                handleFileClick(action.item)
            }
        }
    }

    private fun handleFileClick(item: FileItem) {
        if (item.isDirectory) {
            sendEvent(SearchUiEvent.OpenDirectory(item.path))
        } else {
            sendEvent(SearchUiEvent.OpenFile(item.path, item.mimeType))
        }
    }

    private fun executeSearch(query: String, category: SearchCategory) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            updateState { copy(results = emptyList(), isSearching = false, errorMessage = null) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateState { copy(isSearching = true, errorMessage = null) }
            val rootPath = Environment.getExternalStorageDirectory().absolutePath

            searchFilesUseCase(rootPath = rootPath, query = trimmed, category = category)
                .catch { e ->
                    updateState { copy(isSearching = false, errorMessage = e.localizedMessage) }
                }
                .collect { items ->
                    updateState { copy(isSearching = false, results = items) }
                }
        }
    }
}
