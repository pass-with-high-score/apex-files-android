package app.pwhs.apexfilemanager.features.appmanager.detail

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.core.storage.domain.model.ApkInfo

data class ApkDetailUiState(
    val filePath: String = "",
    val apkInfo: ApkInfo? = null,
    val installedVersionName: String? = null,
    val installedVersionCode: Long? = null,
    val isLoading: Boolean = false,
    val isInstalling: Boolean = false,
    val installStatusMessage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val isInstalled: Boolean
        get() = installedVersionName != null

    val isUpgrade: Boolean
        get() = apkInfo != null && installedVersionCode != null && apkInfo.versionCode > installedVersionCode

    val isDowngrade: Boolean
        get() = apkInfo != null && installedVersionCode != null && apkInfo.versionCode < installedVersionCode
}

sealed interface ApkDetailUiAction : UiAction {
    data class LoadApk(val path: String) : ApkDetailUiAction
    data object InstallClick : ApkDetailUiAction
    data object BrowseArchiveClick : ApkDetailUiAction
}

sealed interface ApkDetailUiEvent : UiEvent {
    data class OpenArchive(val path: String) : ApkDetailUiEvent
    data class ShowToast(val message: String) : ApkDetailUiEvent
    data object NavigateBack : ApkDetailUiEvent
}
