package app.pwhs.apexfilemanager.features.home

import android.content.Intent
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume

/**
 * Danh mục tệp trên màn hình Trang chủ.
 */
enum class HomeCategory {
    DOWNLOADS,
    IMAGES,
    VIDEOS,
    AUDIO,
    DOCUMENTS,
    ARCHIVES,
    APKS,
    RECENTS
}

/**
 * Trạng thái giao diện màn hình Trang chủ (Home).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true,
    val volumes: List<StorageVolume> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val privilegedStatus: app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus = app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus(),
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
    data object NetworkClick : HomeUiAction
    data object VaultClick : HomeUiAction
    data object RequestRootClick : HomeUiAction
    data object RequestShizukuClick : HomeUiAction
    data class SwitchAccessModeClick(val mode: app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode) : HomeUiAction
    data class VolumeClick(val volume: StorageVolume) : HomeUiAction
    data class CategoryClick(val category: HomeCategory) : HomeUiAction
    data class RecentFileClick(val item: FileItem) : HomeUiAction
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
    data object NavigateToApkList : HomeUiEvent
    data object NavigateToWifiShare : HomeUiEvent
    data object NavigateToNetwork : HomeUiEvent
    data object NavigateToVault : HomeUiEvent
    data class OpenRecentFile(val item: FileItem) : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
}

