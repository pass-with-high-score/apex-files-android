package app.pwhs.apexfilemanager.features.viewer.image

import android.graphics.Bitmap
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState

data class ImageViewerUiState(
    val imagePath: String = "",
    val fileName: String = "",
    val bitmap: Bitmap? = null,
    val width: Int = 0,
    val height: Int = 0,
    val fileSizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val rotationDegrees: Float = 0f,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val isInfoDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface ImageViewerUiAction : UiAction {
    data class LoadImage(val path: String) : ImageViewerUiAction
    data object RotateRight : ImageViewerUiAction
    data object ResetTransform : ImageViewerUiAction
    data class UpdateTransform(
        val scaleChange: Float,
        val offsetChangeX: Float,
        val offsetChangeY: Float
    ) : ImageViewerUiAction
    data object ToggleInfoDialog : ImageViewerUiAction
}

sealed interface ImageViewerUiEvent : UiEvent {
    data object NavigateBack : ImageViewerUiEvent
    data class ShowToast(val message: String) : ImageViewerUiEvent
}
