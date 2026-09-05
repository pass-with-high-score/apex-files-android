package app.pwhs.apexfilemanager.features.viewer.hex

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Locale

class HexViewerViewModel :
    BaseViewModel<HexViewerUiState, HexViewerUiAction, HexViewerUiEvent>(HexViewerUiState()) {

    companion object {
        private const val MAX_READ_BYTES = 256 * 1024 // 256 KB preview limit for instant UI rendering
    }

    override fun onAction(action: HexViewerUiAction) {
        when (action) {
            is HexViewerUiAction.LoadFile -> loadFile(action.path)
            is HexViewerUiAction.CopyRow -> copyRow(action.row)
        }
    }

    private fun loadFile(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            try {
                val file = File(path)
                if (!file.exists() || !file.canRead()) {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "Tệp không tồn tại hoặc không thể đọc"
                        )
                    }
                    return@launch
                }

                val totalLength = file.length()
                val readLength = minOf(totalLength, MAX_READ_BYTES.toLong()).toInt()

                val rows = withContext(Dispatchers.IO) {
                    val buffer = ByteArray(readLength)
                    FileInputStream(file).use { fis ->
                        val bytesRead = fis.read(buffer, 0, readLength)
                        if (bytesRead <= 0) return@withContext emptyList<HexRow>()
                        parseBufferToHexRows(buffer, bytesRead)
                    }
                }

                updateState {
                    copy(
                        filePath = path,
                        fileName = file.name,
                        rows = rows,
                        totalBytes = totalLength,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Lỗi nạp dữ liệu hex"
                    )
                }
            }
        }
    }

    private fun parseBufferToHexRows(buffer: ByteArray, length: Int): List<HexRow> {
        val rows = ArrayList<HexRow>((length + 15) / 16)
        var offset = 0
        while (offset < length) {
            val chunkLength = minOf(16, length - offset)
            val offsetStr = String.format(Locale.US, "%08X", offset)

            val hexBuilder = StringBuilder(48)
            val asciiBuilder = StringBuilder(16)

            for (i in 0 until 16) {
                if (i < chunkLength) {
                    val b = buffer[offset + i].toInt() and 0xFF
                    hexBuilder.append(String.format(Locale.US, "%02X ", b))
                    val ch = b.toChar()
                    if (ch in ' '..'~') {
                        asciiBuilder.append(ch)
                    } else {
                        asciiBuilder.append('.')
                    }
                } else {
                    hexBuilder.append("   ")
                }
                if (i == 7) {
                    hexBuilder.append(' ')
                }
            }

            rows.add(
                HexRow(
                    offset = offset.toLong(),
                    offsetHex = offsetStr,
                    hexFormatted = hexBuilder.toString().trimEnd(),
                    asciiText = asciiBuilder.toString()
                )
            )
            offset += chunkLength
        }
        return rows
    }

    private fun copyRow(row: HexRow) {
        val text = "${row.offsetHex}  ${row.hexFormatted}  |${row.asciiText}|"
        sendEvent(HexViewerUiEvent.ShowToast(text))
    }
}
