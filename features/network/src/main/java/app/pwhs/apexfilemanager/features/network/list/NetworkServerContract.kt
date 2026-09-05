package app.pwhs.apexfilemanager.features.network.list

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer

data class NetworkServerUiState(
    val servers: List<NetworkServer> = emptyList(),
    val isLoading: Boolean = false,
    val isTesting: Boolean = false,
    val editingServer: NetworkServer? = null,
    val isEditDialogOpen: Boolean = false
) : UiState

sealed interface NetworkServerUiAction : UiAction {
    data object AddServerClick : NetworkServerUiAction
    data class EditServerClick(val server: NetworkServer) : NetworkServerUiAction
    data class DeleteServer(val id: String) : NetworkServerUiAction
    data class SaveServer(val server: NetworkServer) : NetworkServerUiAction
    data class TestConnection(val server: NetworkServer) : NetworkServerUiAction
    data class ServerClick(val server: NetworkServer) : NetworkServerUiAction
    data object DismissDialog : NetworkServerUiAction
}

sealed interface NetworkServerUiEvent : UiEvent {
    data class ShowToast(val message: String) : NetworkServerUiEvent
    data class NavigateToRemoteExplorer(val serverId: String) : NetworkServerUiEvent
    data object NavigateBack : NetworkServerUiEvent
}
