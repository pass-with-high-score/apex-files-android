package app.pwhs.apexfilemanager.features.wifishare

import androidx.compose.ui.graphics.ImageBitmap
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState

data class WifiShareUiState(
    val ipAddress: String = "",
    val port: Int = 8080,
    val serverUrl: String = "",
    val isServerRunning: Boolean = false,
    val isStarting: Boolean = false,
    val wifiSsid: String = "",
    val qrBitmap: ImageBitmap? = null,
    val errorMessage: String? = null
) : UiState

sealed interface WifiShareUiAction : UiAction {
    data object ToggleServer : WifiShareUiAction
    data object RefreshWifiInfo : WifiShareUiAction
    data object CopyUrl : WifiShareUiAction
}

sealed interface WifiShareUiEvent : UiEvent {
    data class ShowToast(val message: String) : WifiShareUiEvent
    data class CopyTextToClipboard(val text: String) : WifiShareUiEvent
    data object NavigateBack : WifiShareUiEvent
}
