package app.pwhs.apexfilemanager.features.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.data.compat.StorageManagerCompat
import android.os.Environment
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CheckPrivilegedStatusUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetPrivilegedStatusUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetRecentFilesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetStorageVolumesUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RequestRootAccessUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.RequestShizukuAccessUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.SwitchAccessModeUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý logic màn hình Trang chủ (Home).
 */
class HomeViewModel(
    application: Application,
    private val getStorageVolumesUseCase: GetStorageVolumesUseCase,
    private val getRecentFilesUseCase: GetRecentFilesUseCase,
    private val getPrivilegedStatusUseCase: GetPrivilegedStatusUseCase,
    private val checkPrivilegedStatusUseCase: CheckPrivilegedStatusUseCase,
    private val requestRootAccessUseCase: RequestRootAccessUseCase,
    private val requestShizukuAccessUseCase: RequestShizukuAccessUseCase,
    private val switchAccessModeUseCase: SwitchAccessModeUseCase
) : BaseViewModel<HomeUiState, HomeUiAction, HomeUiEvent>(HomeUiState()) {

    private val context = application.applicationContext

    init {
        observePrivilegedStatus()
        checkPermissionAndLoadData()
    }

    override fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.Refresh -> loadAllData()
            is HomeUiAction.CheckPermission -> checkPermissionAndLoadData()
            is HomeUiAction.RequestPermissionClick -> {
                val intent = StorageManagerCompat.createManageAllFilesIntent(context)
                sendEvent(HomeUiEvent.RequestPermission(intent))
            }
            is HomeUiAction.SearchClick -> {
                sendEvent(HomeUiEvent.NavigateToSearch)
            }
            is HomeUiAction.RecentsClick -> {
                sendEvent(HomeUiEvent.NavigateToRecents)
            }
            is HomeUiAction.TrashClick -> {
                sendEvent(HomeUiEvent.NavigateToTrash)
            }
            is HomeUiAction.CleanerClick -> {
                sendEvent(HomeUiEvent.NavigateToCleaner)
            }
            is HomeUiAction.AppsClick -> {
                sendEvent(HomeUiEvent.NavigateToApps)
            }
            is HomeUiAction.WifiShareClick -> {
                sendEvent(HomeUiEvent.NavigateToWifiShare)
            }
            is HomeUiAction.NetworkClick -> {
                sendEvent(HomeUiEvent.NavigateToNetwork)
            }
            is HomeUiAction.VaultClick -> {
                sendEvent(HomeUiEvent.NavigateToVault)
            }
            is HomeUiAction.SettingsClick -> {
                sendEvent(HomeUiEvent.NavigateToSettings)
            }
            is HomeUiAction.RequestRootClick -> {
                viewModelScope.launch {
                    val granted = requestRootAccessUseCase()
                    if (granted) {
                        sendEvent(HomeUiEvent.ShowToast("Đã cấp quyền Root thành công!"))
                        loadStorageVolumes()
                    } else {
                        sendEvent(HomeUiEvent.ShowToast("Không thể cấp quyền Root. Hãy kiểm tra Magisk/KernelSU."))
                    }
                }
            }
            is HomeUiAction.RequestShizukuClick -> {
                viewModelScope.launch {
                    val granted = requestShizukuAccessUseCase()
                    if (granted) {
                        sendEvent(HomeUiEvent.ShowToast("Đã kích hoạt quyền Shizuku thành công!"))
                    } else {
                        sendEvent(HomeUiEvent.ShowToast("Không thể kết nối Shizuku. Hãy mở ứng dụng Shizuku."))
                    }
                }
            }
            is HomeUiAction.SwitchAccessModeClick -> {
                viewModelScope.launch {
                    switchAccessModeUseCase(action.mode)
                }
            }
            is HomeUiAction.VolumeClick -> {
                sendEvent(HomeUiEvent.NavigateToExplorer(action.volume.path))
            }
            is HomeUiAction.CategoryClick -> {
                handleCategoryClick(action.category)
            }
            is HomeUiAction.RecentFileClick -> {
                sendEvent(HomeUiEvent.OpenRecentFile(action.item))
            }
        }
    }

    private fun handleCategoryClick(category: HomeCategory) {
        val rootPath = Environment.getExternalStorageDirectory().absolutePath
        when (category) {
            HomeCategory.DOWNLOADS -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                sendEvent(HomeUiEvent.NavigateToExplorer(path))
            }
            HomeCategory.IMAGES -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
                sendEvent(HomeUiEvent.NavigateToExplorer(path))
            }
            HomeCategory.VIDEOS -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
                sendEvent(HomeUiEvent.NavigateToExplorer(path))
            }
            HomeCategory.AUDIO -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
                sendEvent(HomeUiEvent.NavigateToExplorer(path))
            }
            HomeCategory.DOCUMENTS -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
                sendEvent(HomeUiEvent.NavigateToExplorer(path))
            }
            HomeCategory.ARCHIVES -> {
                sendEvent(HomeUiEvent.NavigateToSearch)
            }
            HomeCategory.APKS -> {
                sendEvent(HomeUiEvent.NavigateToApkList)
            }
            HomeCategory.RECENTS -> {
                sendEvent(HomeUiEvent.NavigateToRecents)
            }
        }
    }

    private fun observePrivilegedStatus() {
        viewModelScope.launch {
            getPrivilegedStatusUseCase().collect { status ->
                updateState { copy(privilegedStatus = status) }
                if (status.isRootGranted) {
                    loadStorageVolumes()
                }
            }
        }
    }

    private fun checkPermissionAndLoadData() {
        val hasPermission = StorageManagerCompat.hasAllFilesAccess()
        updateState { copy(hasPermission = hasPermission) }
        if (hasPermission) {
            loadAllData()
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            checkPrivilegedStatusUseCase()
        }
        loadStorageVolumes()
        loadRecentFiles()
    }

    private fun loadStorageVolumes() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            getStorageVolumesUseCase()
                .catch { e ->
                    updateState { copy(isLoading = false, errorMessage = e.localizedMessage) }
                }
                .collect { volumes ->
                    updateState { copy(isLoading = false, volumes = volumes) }
                }
        }
    }

    private fun loadRecentFiles() {
        viewModelScope.launch {
            getRecentFilesUseCase(10)
                .catch { /* ignore error silently for recents preview */ }
                .collect { files ->
                    updateState { copy(recentFiles = files) }
                }
        }
    }
}
