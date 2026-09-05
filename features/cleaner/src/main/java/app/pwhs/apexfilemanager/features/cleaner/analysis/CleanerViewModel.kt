package app.pwhs.apexfilemanager.features.cleaner.analysis

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.AnalyzeStorageUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CleanJunkFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetJunkFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetLargeFilesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CleanerViewModel(
    private val analyzeStorageUseCase: AnalyzeStorageUseCase,
    private val getLargeFilesUseCase: GetLargeFilesUseCase,
    private val getJunkFilesUseCase: GetJunkFilesUseCase,
    private val cleanJunkFilesUseCase: CleanJunkFilesUseCase
) : BaseViewModel<CleanerUiState, CleanerUiAction, CleanerUiEvent>(CleanerUiState()) {

    init {
        loadData()
    }

    override fun onAction(action: CleanerUiAction) {
        when (action) {
            is CleanerUiAction.LoadData -> loadData()
            is CleanerUiAction.SelectTab -> updateState { copy(selectedTab = action.index) }
            is CleanerUiAction.ToggleSelectJunk -> toggleSelectJunk(action.path)
            is CleanerUiAction.SelectAllJunk -> selectAllJunk()
            is CleanerUiAction.CleanSelectedJunk -> cleanSelectedJunk()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }

            launch {
                analyzeStorageUseCase()
                    .catch { /* ignore */ }
                    .collect { analysis ->
                        updateState { copy(analysis = analysis) }
                    }
            }

            launch {
                getLargeFilesUseCase()
                    .catch { /* ignore */ }
                    .collect { large ->
                        updateState { copy(largeFiles = large) }
                    }
            }

            launch {
                getJunkFilesUseCase()
                    .catch { /* ignore */ }
                    .collect { junk ->
                        updateState {
                            copy(
                                junkFiles = junk,
                                selectedJunkPaths = junk.map { it.path }.toSet(),
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    private fun toggleSelectJunk(path: String) {
        val current = currentState.selectedJunkPaths.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        updateState { copy(selectedJunkPaths = current) }
    }

    private fun selectAllJunk() {
        val allPaths = currentState.junkFiles.map { it.path }.toSet()
        updateState {
            copy(selectedJunkPaths = if (selectedJunkPaths.size == allPaths.size) emptySet() else allPaths)
        }
    }

    private fun cleanSelectedJunk() {
        val toClean = currentState.selectedJunkPaths.toList()
        if (toClean.isEmpty()) return

        viewModelScope.launch {
            updateState { copy(isCleaning = true) }
            val result = cleanJunkFilesUseCase(toClean)
            result.onSuccess {
                sendEvent(CleanerUiEvent.ShowToast("Đã dọn dẹp các tệp rác đã chọn"))
                loadData()
            }.onFailure { e ->
                sendEvent(CleanerUiEvent.ShowToast("Dọn dẹp thất bại: ${e.localizedMessage}"))
            }
            updateState { copy(isCleaning = false) }
        }
    }
}
