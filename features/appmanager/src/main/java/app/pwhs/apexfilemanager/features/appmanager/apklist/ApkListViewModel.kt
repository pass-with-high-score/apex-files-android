package app.pwhs.apexfilemanager.features.appmanager.apklist

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetApkFilesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ApkListViewModel(
    private val getApkFilesUseCase: GetApkFilesUseCase
) : BaseViewModel<ApkListUiState, ApkListUiAction, ApkListUiEvent>(ApkListUiState()) {

    init {
        loadApkFiles()
    }

    override fun onAction(action: ApkListUiAction) {
        when (action) {
            is ApkListUiAction.LoadApkFiles -> loadApkFiles()
            is ApkListUiAction.SearchQueryChanged -> updateState { copy(searchQuery = action.query) }
            is ApkListUiAction.ApkFileClick -> sendEvent(ApkListUiEvent.OpenApkDetail(action.item.path))
        }
    }

    private fun loadApkFiles() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            getApkFilesUseCase()
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { files ->
                    updateState { copy(isLoading = false, apkFiles = files, errorMessage = null) }
                }
        }
    }
}
