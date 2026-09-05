package app.pwhs.apexfilemanager.features.viewer.hex

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState

data class HexRow(
    val offset: Long,
    val offsetHex: String,
    val hexFormatted: String,
    val asciiText: String
)

data class HexViewerUiState(
    val filePath: String = "",
    val fileName: String = "",
    val rows: List<HexRow> = emptyList(),
    val totalBytes: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bytesPerLine: Int = 16
) : UiState

sealed interface HexViewerUiAction : UiAction {
    data class LoadFile(val path: String) : HexViewerUiAction
    data class CopyRow(val row: HexRow) : HexViewerUiAction
}

sealed interface HexViewerUiEvent : UiEvent {
    data class ShowToast(val message: String) : HexViewerUiEvent
    data object NavigateBack : HexViewerUiEvent
}
