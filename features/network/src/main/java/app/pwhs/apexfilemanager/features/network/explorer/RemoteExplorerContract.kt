package app.pwhs.apexfilemanager.features.network.explorer

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem

data class RemoteExplorerUiState(
    val server: NetworkServer? = null,
    val currentPath: String = "",
    val items: List<RemoteFileItem> = emptyList(),
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadingFileName: String? = null,
    val errorMessage: String? = null
) : UiState

sealed interface RemoteExplorerUiAction : UiAction {
    data class Init(val serverId: String) : RemoteExplorerUiAction
    data class ItemClick(val item: RemoteFileItem) : RemoteExplorerUiAction
    data class DownloadItem(val item: RemoteFileItem) : RemoteExplorerUiAction
    data object NavigateUp : RemoteExplorerUiAction
    data object Refresh : RemoteExplorerUiAction
}

sealed interface RemoteExplorerUiEvent : UiEvent {
    data class ShowToast(val message: String) : RemoteExplorerUiEvent
    data object NavigateBack : RemoteExplorerUiEvent
}
