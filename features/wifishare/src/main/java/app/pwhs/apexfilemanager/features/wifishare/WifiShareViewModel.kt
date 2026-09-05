package app.pwhs.apexfilemanager.features.wifishare

import android.app.Application
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.features.wifishare.qr.QrCodeGenerator
import app.pwhs.apexfilemanager.features.wifishare.service.WifiShareService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class WifiShareViewModel(
    private val application: Application
) : BaseViewModel<WifiShareUiState, WifiShareUiAction, WifiShareUiEvent>(WifiShareUiState()) {

    init {
        refreshNetworkInfo()
        observeServerState()
    }

    override fun onAction(action: WifiShareUiAction) {
        when (action) {
            is WifiShareUiAction.ToggleServer -> toggleServer()
            is WifiShareUiAction.RefreshWifiInfo -> refreshNetworkInfo()
            is WifiShareUiAction.CopyUrl -> copyUrl()
        }
    }

    private fun observeServerState() {
        viewModelScope.launch {
            WifiShareService.serverState.collect { status ->
                when (status) {
                    is WifiShareService.ServerStatus.Stopped -> {
                        updateState {
                            copy(
                                isServerRunning = false,
                                isStarting = false,
                                serverUrl = "",
                                qrBitmap = null,
                                errorMessage = null
                            )
                        }
                    }
                    is WifiShareService.ServerStatus.Starting -> {
                        updateState { copy(isStarting = true, errorMessage = null) }
                    }
                    is WifiShareService.ServerStatus.Running -> {
                        val qr = withContext(Dispatchers.Default) {
                            QrCodeGenerator.generateQrBitmap(status.url)
                        }
                        updateState {
                            copy(
                                isServerRunning = true,
                                isStarting = false,
                                serverUrl = status.url,
                                qrBitmap = qr,
                                errorMessage = null
                            )
                        }
                    }
                    is WifiShareService.ServerStatus.Error -> {
                        updateState {
                            copy(
                                isServerRunning = false,
                                isStarting = false,
                                errorMessage = status.message
                            )
                        }
                        sendEvent(WifiShareUiEvent.ShowToast("Lỗi máy chủ: ${status.message}"))
                    }
                }
            }
        }
    }

    private fun toggleServer() {
        val running = currentState.isServerRunning
        if (running) {
            WifiShareService.stop(application)
        } else {
            val ip = currentState.ipAddress.ifBlank { getLocalIpAddress() }
            if (ip.isBlank()) {
                sendEvent(WifiShareUiEvent.ShowToast("Vui lòng kết nối mạng Wi-Fi trước khi bật chia sẻ"))
                return
            }
            updateState { copy(ipAddress = ip) }
            WifiShareService.start(application, ip, currentState.port)
        }
    }

    private fun refreshNetworkInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = getLocalIpAddress()
            updateState { copy(ipAddress = ip) }
        }
    }

    private fun copyUrl() {
        val url = currentState.serverUrl
        if (url.isNotBlank()) {
            sendEvent(WifiShareUiEvent.CopyTextToClipboard(url))
        }
    }

    private fun getLocalIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return ""
            for (intf in interfaces.asSequence()) {
                for (addr in intf.inetAddresses.asSequence()) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
            ""
        } catch (_: Exception) {
            ""
        }
    }
}
