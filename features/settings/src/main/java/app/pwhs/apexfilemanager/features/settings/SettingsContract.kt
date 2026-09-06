package app.pwhs.apexfilemanager.features.settings

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.AppSettings
import app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus
import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val cacheSizeBytes: Long = 0L,
    val privilegedStatus: PrivilegedStatus = PrivilegedStatus(),
    val isClearingCache: Boolean = false,
    val showThemeDialog: Boolean = false,
    val appVersion: String = "1.0.0"
) : UiState

sealed interface SettingsUiAction : UiAction {
    data class ToggleThemeDialog(val show: Boolean) : SettingsUiAction
    data class SelectTheme(val themeMode: ThemeMode) : SettingsUiAction
    data class ToggleDynamicColor(val enabled: Boolean) : SettingsUiAction
    data class ToggleShowHiddenFiles(val show: Boolean) : SettingsUiAction
    data class ToggleShowFileExtensions(val show: Boolean) : SettingsUiAction
    data object ClearCache : SettingsUiAction
    data object ManagePermissions : SettingsUiAction
    data object BackClick : SettingsUiAction
}

sealed interface SettingsUiEvent : UiEvent {
    data object NavigateBack : SettingsUiEvent
    data object OpenSystemPermissionSettings : SettingsUiEvent
    data class ShowToast(val message: String) : SettingsUiEvent
}
