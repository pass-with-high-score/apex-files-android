package app.pwhs.apexfilemanager.features.cleaner.trash

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteTrashPermanentlyUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.EmptyTrashUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetTrashItemsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RestoreTrashItemUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TrashViewModel(
    private val getTrashItemsUseCase: GetTrashItemsUseCase,
    private val restoreTrashItemUseCase: RestoreTrashItemUseCase,
    private val deleteTrashPermanentlyUseCase: DeleteTrashPermanentlyUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase
) : BaseViewModel<TrashUiState, TrashUiAction, TrashUiEvent>(TrashUiState()) {

    init {
        loadTrash()
    }

    override fun onAction(action: TrashUiAction) {
        when (action) {
            is TrashUiAction.LoadTrash -> loadTrash()
            is TrashUiAction.RestoreItem -> restoreItem(action.item.id)
            is TrashUiAction.ConfirmDeleteSingle -> updateState { copy(selectedItemForDelete = action.item) }
            is TrashUiAction.DismissDeleteSingle -> updateState { copy(selectedItemForDelete = null) }
            is TrashUiAction.DeletePermanentlyConfirmed -> deleteSinglePermanently()
            is TrashUiAction.ConfirmEmptyTrash -> updateState { copy(isEmptyTrashConfirmVisible = true) }
            is TrashUiAction.DismissEmptyTrash -> updateState { copy(isEmptyTrashConfirmVisible = false) }
            is TrashUiAction.EmptyTrashConfirmed -> emptyTrash()
        }
    }

    private fun loadTrash() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            getTrashItemsUseCase()
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { items ->
                    updateState { copy(isLoading = false, items = items, errorMessage = null) }
                }
        }
    }

    private fun restoreItem(trashId: String) {
        viewModelScope.launch {
            val result = restoreTrashItemUseCase(trashId)
            result.onSuccess {
                sendEvent(TrashUiEvent.ShowToast("Đã khôi phục tệp thành công"))
                loadTrash()
            }.onFailure { e ->
                sendEvent(TrashUiEvent.ShowToast("Khôi phục thất bại: ${e.localizedMessage}"))
            }
        }
    }

    private fun deleteSinglePermanently() {
        val item = currentState.selectedItemForDelete ?: return
        updateState { copy(selectedItemForDelete = null) }
        viewModelScope.launch {
            val result = deleteTrashPermanentlyUseCase(item.id)
            result.onSuccess {
                sendEvent(TrashUiEvent.ShowToast("Đã xóa vĩnh viễn tệp"))
                loadTrash()
            }.onFailure { e ->
                sendEvent(TrashUiEvent.ShowToast("Xóa thất bại: ${e.localizedMessage}"))
            }
        }
    }

    private fun emptyTrash() {
        updateState { copy(isEmptyTrashConfirmVisible = false) }
        viewModelScope.launch {
            val result = emptyTrashUseCase()
            result.onSuccess {
                sendEvent(TrashUiEvent.ShowToast("Đã dọn sạch thùng rác"))
                loadTrash()
            }.onFailure { e ->
                sendEvent(TrashUiEvent.ShowToast("Dọn thùng rác thất bại: ${e.localizedMessage}"))
            }
        }
    }
}
