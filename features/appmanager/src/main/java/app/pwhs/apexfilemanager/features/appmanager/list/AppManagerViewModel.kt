package app.pwhs.apexfilemanager.features.appmanager.list

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.model.InstalledApp
import app.pwhs.apexfilemanager.core.storage.domain.usecase.BackupAppUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AppManagerViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val backupAppUseCase: BackupAppUseCase
) : BaseViewModel<AppManagerUiState, AppManagerUiAction, AppManagerUiEvent>(AppManagerUiState()) {

    init {
        loadApps()
    }

    override fun onAction(action: AppManagerUiAction) {
        when (action) {
            is AppManagerUiAction.LoadApps -> loadApps()
            is AppManagerUiAction.SelectTab -> updateState { copy(selectedTab = action.index) }
            is AppManagerUiAction.SearchQueryChanged -> updateState { copy(searchQuery = action.query) }
            is AppManagerUiAction.BackupApp -> backupApp(action.app)
            is AppManagerUiAction.ShareApp -> shareApp(action.app)
            is AppManagerUiAction.LaunchApp -> sendEvent(AppManagerUiEvent.LaunchAppIntent(action.app.packageName))
            is AppManagerUiAction.UninstallApp -> sendEvent(AppManagerUiEvent.UninstallAppIntent(action.app.packageName))
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            getInstalledAppsUseCase(includeSystem = true)
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { allApps ->
                    val user = allApps.filter { !it.isSystemApp }
                    val system = allApps.filter { it.isSystemApp }
                    updateState {
                        copy(
                            isLoading = false,
                            userApps = user,
                            systemApps = system,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun backupApp(app: InstalledApp) {
        viewModelScope.launch {
            updateState { copy(backingUpPackage = app.packageName) }
            val result = backupAppUseCase(app.packageName)
            result.onSuccess { backupPath ->
                sendEvent(AppManagerUiEvent.ShowToast("Đã sao lưu APK tại: $backupPath"))
            }.onFailure { e ->
                sendEvent(AppManagerUiEvent.ShowToast("Sao lưu thất bại: ${e.localizedMessage}"))
            }
            updateState { copy(backingUpPackage = null) }
        }
    }

    private fun shareApp(app: InstalledApp) {
        sendEvent(AppManagerUiEvent.ShareApkFile(app.apkPath, app.appName))
    }
}
