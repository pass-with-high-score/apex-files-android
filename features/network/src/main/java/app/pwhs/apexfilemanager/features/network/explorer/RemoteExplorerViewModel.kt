package app.pwhs.apexfilemanager.features.network.explorer

import android.os.Environment
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository
import app.pwhs.apexfilemanager.core.storage.domain.usecase.DownloadRemoteFileUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ListRemoteFilesUseCase
import kotlinx.coroutines.launch
import java.io.File

class RemoteExplorerViewModel(
    private val repository: NetworkServerRepository,
    private val listRemoteFilesUseCase: ListRemoteFilesUseCase,
    private val downloadRemoteFileUseCase: DownloadRemoteFileUseCase
) : BaseViewModel<RemoteExplorerUiState, RemoteExplorerUiAction, RemoteExplorerUiEvent>(RemoteExplorerUiState()) {

    override fun onAction(action: RemoteExplorerUiAction) {
        when (action) {
            is RemoteExplorerUiAction.Init -> initServer(action.serverId)
            is RemoteExplorerUiAction.ItemClick -> handleItemClick(action.item)
            is RemoteExplorerUiAction.DownloadItem -> downloadFile(action.item)
            is RemoteExplorerUiAction.NavigateUp -> navigateUp()
            is RemoteExplorerUiAction.Refresh -> refresh()
        }
    }

    private fun initServer(serverId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            val server = repository.getServerById(serverId)
            if (server == null) {
                updateState { copy(isLoading = false, errorMessage = "Không tìm thấy máy chủ") }
                sendEvent(RemoteExplorerUiEvent.ShowToast("Không tìm thấy thông tin máy chủ"))
                return@launch
            }
            val initialPath = server.shareOrPath.ifBlank { "/" }
            updateState { copy(server = server, currentPath = initialPath) }
            loadDirectory(server, initialPath)
        }
    }

    private fun loadDirectory(server: NetworkServer, path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null, currentPath = path) }
            val result = listRemoteFilesUseCase(server, path)
            result.onSuccess { files ->
                updateState { copy(isLoading = false, items = files) }
            }.onFailure { error ->
                updateState { copy(isLoading = false, errorMessage = error.localizedMessage) }
                sendEvent(RemoteExplorerUiEvent.ShowToast("Không thể tải thư mục: ${error.localizedMessage}"))
            }
        }
    }

    private fun handleItemClick(item: RemoteFileItem) {
        if (item.isDirectory) {
            val server = currentState.server ?: return
            loadDirectory(server, item.path)
        } else {
            downloadFile(item)
        }
    }

    private fun downloadFile(item: RemoteFileItem) {
        val server = currentState.server ?: return
        viewModelScope.launch {
            updateState { copy(isDownloading = true, downloadingFileName = item.name) }
            sendEvent(RemoteExplorerUiEvent.ShowToast("Bắt đầu tải xuống: ${item.name}"))

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloadsDir, item.name)

            val result = downloadRemoteFileUseCase(server, item.path, destFile)
            updateState { copy(isDownloading = false, downloadingFileName = null) }

            result.onSuccess {
                sendEvent(RemoteExplorerUiEvent.ShowToast("Đã tải về thư mục Tải xuống: ${item.name}"))
            }.onFailure { error ->
                sendEvent(RemoteExplorerUiEvent.ShowToast("Tải xuống thất bại: ${error.localizedMessage}"))
            }
        }
    }

    private fun navigateUp() {
        val server = currentState.server ?: return
        val current = currentState.currentPath
        val parent = if (current.contains('/')) {
            val idx = current.lastIndexOf('/')
            if (idx <= 0) "/" else current.substring(0, idx)
        } else {
            "/"
        }

        if (current == "/" || current.isBlank()) {
            sendEvent(RemoteExplorerUiEvent.NavigateBack)
        } else {
            loadDirectory(server, parent)
        }
    }

    private fun refresh() {
        val server = currentState.server ?: return
        loadDirectory(server, currentState.currentPath)
    }
}
