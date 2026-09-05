package app.pwhs.apexfilemanager.features.appmanager.detail

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetApkInfoUseCase
import kotlinx.coroutines.launch
import ru.solrudev.ackpine.installer.InstallFailure
import ru.solrudev.ackpine.installer.PackageInstaller
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.await
import java.io.File

class ApkDetailViewModel(
    private val application: Application,
    private val getApkInfoUseCase: GetApkInfoUseCase
) : BaseViewModel<ApkDetailUiState, ApkDetailUiAction, ApkDetailUiEvent>(ApkDetailUiState()) {

    private val packageInstaller by lazy {
        PackageInstaller.getInstance(application)
    }

    override fun onAction(action: ApkDetailUiAction) {
        when (action) {
            is ApkDetailUiAction.LoadApk -> loadApk(action.path)
            is ApkDetailUiAction.InstallClick -> installApk()
            is ApkDetailUiAction.BrowseArchiveClick -> {
                val path = currentState.filePath
                if (path.isNotEmpty()) {
                    sendEvent(ApkDetailUiEvent.OpenArchive(path))
                }
            }
        }
    }

    private fun loadApk(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null, filePath = path) }
            val result = getApkInfoUseCase(path)
            result.onSuccess { apkInfo ->
                val (installedVersion, installedCode) = checkInstalledApp(apkInfo.packageName)
                updateState {
                    copy(
                        isLoading = false,
                        apkInfo = apkInfo,
                        installedVersionName = installedVersion,
                        installedVersionCode = installedCode
                    )
                }
            }.onFailure { e ->
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Không thể phân tích gói APK"
                    )
                }
            }
        }
    }

    private fun checkInstalledApp(packageName: String): Pair<String?, Long?> {
        return try {
            val pm = application.packageManager
            val pkgInfo = pm.getPackageInfo(packageName, 0)
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }
            Pair(pkgInfo.versionName, code)
        } catch (_: PackageManager.NameNotFoundException) {
            Pair(null, null)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    private fun installApk() {
        val path = currentState.filePath
        if (path.isEmpty()) return

        val file = File(path)
        if (!file.exists()) {
            sendEvent(ApkDetailUiEvent.ShowToast("Tệp APK không tồn tại"))
            return
        }

        viewModelScope.launch {
            updateState { copy(isInstalling = true) }
            try {
                val uri = Uri.fromFile(file)
                val session = packageInstaller.createSession(uri)

                when (val result = session.await()) {
                    is Session.State.Succeeded -> {
                        updateState { copy(isInstalling = false) }
                        sendEvent(ApkDetailUiEvent.ShowToast("Cài đặt ứng dụng thành công"))
                        currentState.apkInfo?.let {
                            val (newVer, newCode) = checkInstalledApp(it.packageName)
                            updateState { copy(installedVersionName = newVer, installedVersionCode = newCode) }
                        }
                    }
                    is Session.State.Failed -> {
                        updateState { copy(isInstalling = false) }
                        val message = when (val failure = result.failure) {
                            is InstallFailure.Aborted -> "Đã hủy cài đặt"
                            is InstallFailure.Blocked -> "Cài đặt bị chặn"
                            is InstallFailure.Conflict -> "Xung đột với ứng dụng đang có"
                            is InstallFailure.Incompatible -> "Gói cài đặt không tương thích với thiết bị"
                            is InstallFailure.Invalid -> "Gói cài đặt APK không hợp lệ"
                            is InstallFailure.Storage -> "Không đủ dung lượng bộ nhớ"
                            else -> failure.message ?: "Lỗi không xác định"
                        }
                        sendEvent(ApkDetailUiEvent.ShowToast("Cài đặt thất bại: $message"))
                    }
                }
            } catch (e: Exception) {
                updateState { copy(isInstalling = false) }
                sendEvent(ApkDetailUiEvent.ShowToast("Lỗi phiên cài đặt: ${e.localizedMessage}"))
            }
        }
    }
}
