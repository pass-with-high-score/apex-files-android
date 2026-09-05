package app.pwhs.apexfilemanager.features.appmanager.list

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.InstalledApp

data class AppManagerUiState(
    val userApps: List<InstalledApp> = emptyList(),
    val systemApps: List<InstalledApp> = emptyList(),
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val backingUpPackage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val currentDisplayApps: List<InstalledApp>
        get() {
            val base = if (selectedTab == 0) userApps else systemApps
            if (searchQuery.isBlank()) return base
            val query = searchQuery.trim().lowercase()
            return base.filter {
                it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }
}

sealed interface AppManagerUiAction : UiAction {
    data object LoadApps : AppManagerUiAction
    data class SelectTab(val index: Int) : AppManagerUiAction
    data class SearchQueryChanged(val query: String) : AppManagerUiAction
    data class BackupApp(val app: InstalledApp) : AppManagerUiAction
    data class ShareApp(val app: InstalledApp) : AppManagerUiAction
    data class LaunchApp(val app: InstalledApp) : AppManagerUiAction
    data class UninstallApp(val app: InstalledApp) : AppManagerUiAction
}

sealed interface AppManagerUiEvent : UiEvent {
    data class ShareApkFile(val apkPath: String, val appName: String) : AppManagerUiEvent
    data class LaunchAppIntent(val packageName: String) : AppManagerUiEvent
    data class UninstallAppIntent(val packageName: String) : AppManagerUiEvent
    data class ShowToast(val message: String) : AppManagerUiEvent
    data object NavigateBack : AppManagerUiEvent
}
