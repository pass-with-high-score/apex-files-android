package app.pwhs.apexfilemanager.features.vault.main

import androidx.annotation.StringRes
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import java.io.File

enum class VaultCategory {
    ALL,
    IMAGES,
    VIDEOS,
    DOCUMENTS,
    OTHERS
}

data class VaultUiState(
    val items: List<VaultItem> = emptyList(),
    val filteredItems: List<VaultItem> = emptyList(),
    val selectedCategory: VaultCategory = VaultCategory.ALL,
    val isLoading: Boolean = false,
    val selectedItemForMenu: VaultItem? = null,
    val itemToDelete: VaultItem? = null,
    val showSettingsDialog: Boolean = false,
    val showChangePinDialog: Boolean = false,
    val isBiometricEnabled: Boolean = false
) : UiState

sealed interface VaultUiAction : UiAction {
    data object LoadItems : VaultUiAction
    data class SelectCategory(val category: VaultCategory) : VaultUiAction
    data class ItemClick(val item: VaultItem) : VaultUiAction
    data class ItemLongClick(val item: VaultItem) : VaultUiAction
    data object DismissMenu : VaultUiAction
    data class ImportFile(val path: String) : VaultUiAction
    data class ExportItem(val item: VaultItem) : VaultUiAction
    data class AskDelete(val item: VaultItem) : VaultUiAction
    data object DismissDeleteConfirm : VaultUiAction
    data object ExecuteDelete : VaultUiAction
    data class ToggleBiometric(val enabled: Boolean) : VaultUiAction
    data class ChangePin(val oldPin: String, val newPin: String) : VaultUiAction
    data object OpenSettings : VaultUiAction
    data object DismissSettings : VaultUiAction
    data object OpenChangePin : VaultUiAction
    data object DismissChangePin : VaultUiAction
}

sealed interface VaultUiEvent : UiEvent {
    data class OpenFile(val file: File, val mimeType: String) : VaultUiEvent
    data class ShowToast(val message: String) : VaultUiEvent
    data class ShowToastRes(@StringRes val messageRes: Int) : VaultUiEvent
}
