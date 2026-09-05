package app.pwhs.apexfilemanager.features.network.list

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteNetworkServerUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetNetworkServersUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.SaveNetworkServerUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.TestNetworkConnectionUseCase
import kotlinx.coroutines.launch

class NetworkServerViewModel(
    private val getNetworkServersUseCase: GetNetworkServersUseCase,
    private val saveNetworkServerUseCase: SaveNetworkServerUseCase,
    private val deleteNetworkServerUseCase: DeleteNetworkServerUseCase,
    private val testNetworkConnectionUseCase: TestNetworkConnectionUseCase
) : BaseViewModel<NetworkServerUiState, NetworkServerUiAction, NetworkServerUiEvent>(NetworkServerUiState()) {

    init {
        loadServers()
    }

    override fun onAction(action: NetworkServerUiAction) {
        when (action) {
            is NetworkServerUiAction.AddServerClick -> {
                updateState { copy(isEditDialogOpen = true, editingServer = null) }
            }
            is NetworkServerUiAction.EditServerClick -> {
                updateState { copy(isEditDialogOpen = true, editingServer = action.server) }
            }
            is NetworkServerUiAction.DismissDialog -> {
                updateState { copy(isEditDialogOpen = false, editingServer = null) }
            }
            is NetworkServerUiAction.SaveServer -> saveServer(action.server)
            is NetworkServerUiAction.DeleteServer -> deleteServer(action.id)
            is NetworkServerUiAction.TestConnection -> testConnection(action.server)
            is NetworkServerUiAction.ServerClick -> {
                sendEvent(NetworkServerUiEvent.NavigateToRemoteExplorer(action.server.id))
            }
        }
    }

    private fun loadServers() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            getNetworkServersUseCase().collect { list ->
                updateState { copy(isLoading = false, servers = list) }
            }
        }
    }

    private fun saveServer(server: NetworkServer) {
        viewModelScope.launch {
            saveNetworkServerUseCase(server)
            updateState { copy(isEditDialogOpen = false, editingServer = null) }
            sendEvent(NetworkServerUiEvent.ShowToast("Đã lưu kết nối mạng"))
        }
    }

    private fun deleteServer(id: String) {
        viewModelScope.launch {
            deleteNetworkServerUseCase(id)
            sendEvent(NetworkServerUiEvent.ShowToast("Đã xóa kết nối mạng"))
        }
    }

    private fun testConnection(server: NetworkServer) {
        viewModelScope.launch {
            updateState { copy(isTesting = true) }
            val result = testNetworkConnectionUseCase(server)
            updateState { copy(isTesting = false) }
            result.onSuccess {
                sendEvent(NetworkServerUiEvent.ShowToast("Kết nối thành công!"))
            }.onFailure { error ->
                sendEvent(NetworkServerUiEvent.ShowToast("Kết nối thất bại: ${error.localizedMessage ?: "Lỗi không xác định"}"))
            }
        }
    }
}
