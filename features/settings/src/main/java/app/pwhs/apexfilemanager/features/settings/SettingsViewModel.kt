package app.pwhs.apexfilemanager.features.settings

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.ClearAppCacheUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetAppCacheSizeUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetPrivilegedStatusUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetSettingsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateDynamicColorUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateShowFileExtensionsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateShowHiddenFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateThemeModeUseCase
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase,
    private val updateDynamicColorUseCase: UpdateDynamicColorUseCase,
    private val updateShowHiddenFilesUseCase: UpdateShowHiddenFilesUseCase,
    private val updateShowFileExtensionsUseCase: UpdateShowFileExtensionsUseCase,
    private val clearAppCacheUseCase: ClearAppCacheUseCase,
    private val getAppCacheSizeUseCase: GetAppCacheSizeUseCase,
    private val getPrivilegedStatusUseCase: GetPrivilegedStatusUseCase
) : BaseViewModel<SettingsUiState, SettingsUiAction, SettingsUiEvent>(SettingsUiState()) {

    init {
        observeSettings()
        observePrivilegedStatus()
        loadCacheSize()
    }

    override fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ToggleThemeDialog -> {
                updateState { copy(showThemeDialog = action.show) }
            }
            is SettingsUiAction.SelectTheme -> {
                viewModelScope.launch {
                    updateThemeModeUseCase(action.themeMode)
                    updateState { copy(showThemeDialog = false) }
                }
            }
            is SettingsUiAction.ToggleDynamicColor -> {
                viewModelScope.launch {
                    updateDynamicColorUseCase(action.enabled)
                }
            }
            is SettingsUiAction.ToggleShowHiddenFiles -> {
                viewModelScope.launch {
                    updateShowHiddenFilesUseCase(action.show)
                }
            }
            is SettingsUiAction.ToggleShowFileExtensions -> {
                viewModelScope.launch {
                    updateShowFileExtensionsUseCase(action.show)
                }
            }
            is SettingsUiAction.ClearCache -> {
                handleClearCache()
            }
            is SettingsUiAction.ManagePermissions -> {
                sendEvent(SettingsUiEvent.OpenSystemPermissionSettings)
            }
            is SettingsUiAction.BackClick -> {
                sendEvent(SettingsUiEvent.NavigateBack)
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                updateState { copy(settings = settings) }
            }
        }
    }

    private fun observePrivilegedStatus() {
        viewModelScope.launch {
            getPrivilegedStatusUseCase().collect { status ->
                updateState { copy(privilegedStatus = status) }
            }
        }
    }

    private fun loadCacheSize() {
        viewModelScope.launch {
            val size = getAppCacheSizeUseCase()
            updateState { copy(cacheSizeBytes = size) }
        }
    }

    private fun handleClearCache() {
        viewModelScope.launch {
            updateState { copy(isClearingCache = true) }
            val cleared = clearAppCacheUseCase()
            val newSize = getAppCacheSizeUseCase()
            updateState { copy(isClearingCache = false, cacheSizeBytes = newSize) }
            if (cleared) {
                sendEvent(SettingsUiEvent.ShowToast("Đã xóa bộ nhớ đệm thành công!"))
            } else {
                sendEvent(SettingsUiEvent.ShowToast("Không thể xóa bộ nhớ đệm"))
            }
        }
    }
}
