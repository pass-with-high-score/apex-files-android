package app.pwhs.apexfilemanager.features.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.data.compat.StorageManagerCompat
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetStorageVolumesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý logic màn hình Trang chủ (Home).
 */
class HomeViewModel(
    application: Application,
    private val getStorageVolumesUseCase: GetStorageVolumesUseCase
) : BaseViewModel<HomeUiState, HomeUiAction, HomeUiEvent>(HomeUiState()) {

    private val context = application.applicationContext

    init {
        checkPermissionAndLoadData()
    }

    override fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.Refresh -> loadStorageVolumes()
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
            is HomeUiAction.VolumeClick -> {
                sendEvent(HomeUiEvent.NavigateToExplorer(action.volume.path))
            }
        }
    }

    private fun checkPermissionAndLoadData() {
        val hasPermission = StorageManagerCompat.hasAllFilesAccess()
        updateState { copy(hasPermission = hasPermission) }
        if (hasPermission) {
            loadStorageVolumes()
        }
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
}
