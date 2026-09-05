package app.pwhs.apexfilemanager.features.home

import android.content.Intent
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume

/**
 * Trạng thái giao diện màn hình Trang chủ (Home).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true,
    val volumes: List<StorageVolume> = emptyList(),
    val errorMessage: String? = null
) : UiState

/**
 * Các hành động của người dùng trên màn hình Trang chủ.
 */
sealed interface HomeUiAction : UiAction {
    data object Refresh : HomeUiAction
    data object CheckPermission : HomeUiAction
    data object RequestPermissionClick : HomeUiAction
    data object SearchClick : HomeUiAction
    data object RecentsClick : HomeUiAction
    data object TrashClick : HomeUiAction
    data object CleanerClick : HomeUiAction
    data object AppsClick : HomeUiAction
    data object WifiShareClick : HomeUiAction
    data class VolumeClick(val volume: StorageVolume) : HomeUiAction
}

/**
 * Các sự kiện một lần (One-time event) từ ViewModel gửi ra ngoài UI.
 */
sealed interface HomeUiEvent : UiEvent {
    data class RequestPermission(val intent: Intent) : HomeUiEvent
    data class NavigateToExplorer(val path: String) : HomeUiEvent
    data object NavigateToSearch : HomeUiEvent
    data object NavigateToRecents : HomeUiEvent
    data object NavigateToTrash : HomeUiEvent
    data object NavigateToCleaner : HomeUiEvent
    data object NavigateToApps : HomeUiEvent
    data object NavigateToWifiShare : HomeUiEvent
}
