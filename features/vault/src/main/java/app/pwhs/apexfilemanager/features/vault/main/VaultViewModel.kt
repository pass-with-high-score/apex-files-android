package app.pwhs.apexfilemanager.features.vault.main

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import app.pwhs.apexfilemanager.core.storage.domain.usecase.AuthenticateVaultUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteVaultItemUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ExportFromVaultUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetVaultDecryptedFileUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetVaultItemsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ImportToVaultUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.SetVaultPinUseCase
import app.pwhs.apexfilemanager.features.vault.R
import kotlinx.coroutines.launch

class VaultViewModel(
    private val getVaultItemsUseCase: GetVaultItemsUseCase,
    private val importToVaultUseCase: ImportToVaultUseCase,
    private val exportFromVaultUseCase: ExportFromVaultUseCase,
    private val deleteVaultItemUseCase: DeleteVaultItemUseCase,
    private val getVaultDecryptedFileUseCase: GetVaultDecryptedFileUseCase,
    private val authenticateVaultUseCase: AuthenticateVaultUseCase,
    private val setVaultPinUseCase: SetVaultPinUseCase
) : BaseViewModel<VaultUiState, VaultUiAction, VaultUiEvent>(
    VaultUiState()
) {

    init {
        loadItems()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val bio = authenticateVaultUseCase.isBiometricEnabled()
            updateState { copy(isBiometricEnabled = bio) }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val items = getVaultItemsUseCase()
            updateState {
                copy(
                    items = items,
                    filteredItems = filterItems(items, selectedCategory),
                    isLoading = false
                )
            }
        }
    }

    override fun onAction(action: VaultUiAction) {
        when (action) {
            is VaultUiAction.LoadItems -> loadItems()
            is VaultUiAction.SelectCategory -> {
                updateState {
                    copy(
                        selectedCategory = action.category,
                        filteredItems = filterItems(items, action.category)
                    )
                }
            }
            is VaultUiAction.ItemClick -> openItem(action.item)
            is VaultUiAction.ItemLongClick -> {
                updateState { copy(selectedItemForMenu = action.item) }
            }
            is VaultUiAction.DismissMenu -> {
                updateState { copy(selectedItemForMenu = null) }
            }
            is VaultUiAction.ImportFile -> importFile(action.path)
            is VaultUiAction.ExportItem -> exportItem(action.item)
            is VaultUiAction.AskDelete -> {
                updateState { copy(itemToDelete = action.item, selectedItemForMenu = null) }
            }
            is VaultUiAction.DismissDeleteConfirm -> {
                updateState { copy(itemToDelete = null) }
            }
            is VaultUiAction.ExecuteDelete -> executeDelete()
            is VaultUiAction.ToggleBiometric -> toggleBiometric(action.enabled)
            is VaultUiAction.ChangePin -> changePin(action.oldPin, action.newPin)
            is VaultUiAction.OpenSettings -> updateState { copy(showSettingsDialog = true) }
            is VaultUiAction.DismissSettings -> updateState { copy(showSettingsDialog = false) }
            is VaultUiAction.OpenChangePin -> updateState { copy(showChangePinDialog = true) }
            is VaultUiAction.DismissChangePin -> updateState { copy(showChangePinDialog = false) }
        }
    }

    private fun openItem(item: VaultItem) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val result = getVaultDecryptedFileUseCase.getTempFile(item.id)
            updateState { copy(isLoading = false) }
            result.onSuccess { tempFile ->
                sendEvent(VaultUiEvent.OpenFile(tempFile, item.mimeType))
            }.onFailure {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_import_failed))
            }
        }
    }

    private fun importFile(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            val result = importToVaultUseCase(path, deleteOriginal = true)
            updateState { copy(isLoading = false) }
            result.onSuccess {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_import_success))
                loadItems()
            }.onFailure {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_import_failed))
            }
        }
    }

    private fun exportItem(item: VaultItem) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, selectedItemForMenu = null) }
            val result = exportFromVaultUseCase(item.id)
            updateState { copy(isLoading = false) }
            result.onSuccess { restoredPath ->
                sendEvent(VaultUiEvent.ShowToast("Đã khôi phục: $restoredPath"))
                loadItems()
            }.onFailure {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_export_failed))
            }
        }
    }

    private fun executeDelete() {
        val item = currentState.itemToDelete ?: return
        viewModelScope.launch {
            updateState { copy(isLoading = true, itemToDelete = null) }
            val result = deleteVaultItemUseCase(item.id)
            updateState { copy(isLoading = false) }
            result.onSuccess {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_delete_success))
                loadItems()
            }.onFailure {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_delete_failed))
            }
        }
    }

    private fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            val result = authenticateVaultUseCase.setBiometricEnabled(enabled)
            if (result.isSuccess) {
                updateState { copy(isBiometricEnabled = enabled) }
            }
        }
    }

    private fun changePin(oldPin: String, newPin: String) {
        viewModelScope.launch {
            val result = setVaultPinUseCase.change(oldPin, newPin)
            if (result.isSuccess) {
                updateState { copy(showChangePinDialog = false) }
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_change_pin_success))
            } else {
                sendEvent(VaultUiEvent.ShowToastRes(R.string.vault_change_pin_failed))
            }
        }
    }

    private fun filterItems(items: List<VaultItem>, category: VaultCategory): List<VaultItem> {
        return when (category) {
            VaultCategory.ALL -> items
            VaultCategory.IMAGES -> items.filter { it.mimeType.startsWith("image/") }
            VaultCategory.VIDEOS -> items.filter { it.mimeType.startsWith("video/") }
            VaultCategory.DOCUMENTS -> items.filter {
                it.mimeType.startsWith("application/pdf") || it.mimeType.startsWith("text/") ||
                        it.originalName.endsWith(".doc", true) || it.originalName.endsWith(".docx", true) ||
                        it.originalName.endsWith(".xls", true) || it.originalName.endsWith(".xlsx", true)
            }
            VaultCategory.OTHERS -> items.filter {
                !it.mimeType.startsWith("image/") &&
                        !it.mimeType.startsWith("video/") &&
                        !it.mimeType.startsWith("application/pdf") &&
                        !it.mimeType.startsWith("text/")
            }
        }
    }
}
