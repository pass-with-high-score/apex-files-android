package app.pwhs.apexfilemanager.features.cleaner.trash

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.TrashItem

data class TrashUiState(
    val items: List<TrashItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedItemForDelete: TrashItem? = null,
    val isEmptyTrashConfirmVisible: Boolean = false
) : UiState {
    val totalSizeBytes: Long
        get() = items.sumOf { it.sizeBytes }
}

sealed interface TrashUiAction : UiAction {
    data object LoadTrash : TrashUiAction
    data class RestoreItem(val item: TrashItem) : TrashUiAction
    data class ConfirmDeleteSingle(val item: TrashItem) : TrashUiAction
    data object DismissDeleteSingle : TrashUiAction
    data object DeletePermanentlyConfirmed : TrashUiAction
    data object ConfirmEmptyTrash : TrashUiAction
    data object DismissEmptyTrash : TrashUiAction
    data object EmptyTrashConfirmed : TrashUiAction
}

sealed interface TrashUiEvent : UiEvent {
    data class ShowToast(val message: String) : TrashUiEvent
    data object NavigateBack : TrashUiEvent
}
