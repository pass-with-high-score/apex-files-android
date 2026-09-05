package app.pwhs.apexfilemanager.features.viewer.image

import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageViewerViewModel :
    BaseViewModel<ImageViewerUiState, ImageViewerUiAction, ImageViewerUiEvent>(ImageViewerUiState()) {

    override fun onAction(action: ImageViewerUiAction) {
        when (action) {
            is ImageViewerUiAction.LoadImage -> loadImage(action.path)
            is ImageViewerUiAction.RotateRight -> rotateRight()
            is ImageViewerUiAction.ResetTransform -> resetTransform()
            is ImageViewerUiAction.UpdateTransform -> updateTransform(
                action.scaleChange,
                action.offsetChangeX,
                action.offsetChangeY
            )
            is ImageViewerUiAction.ToggleInfoDialog -> toggleInfoDialog()
        }
    }

    private fun loadImage(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            try {
                val file = File(path)
                if (!file.exists() || !file.canRead()) {
                    updateState {
                        copy(isLoading = false, errorMessage = "Tệp ảnh không tồn tại")
                    }
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(path, boundsOptions)

                    val origWidth = boundsOptions.outWidth
                    val origHeight = boundsOptions.outHeight

                    if (origWidth <= 0 || origHeight <= 0) {
                        return@withContext null
                    }

                    // Tính sample size an toàn với max dimension 2560px
                    val maxDimension = 2560
                    var sampleSize = 1
                    while (origWidth / sampleSize > maxDimension || origHeight / sampleSize > maxDimension) {
                        sampleSize *= 2
                    }

                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                    }
                    val bitmap = BitmapFactory.decodeFile(path, decodeOptions)
                    Triple(bitmap, origWidth, origHeight)
                }

                if (result?.first != null) {
                    updateState {
                        copy(
                            imagePath = path,
                            fileName = file.name,
                            bitmap = result.first,
                            width = result.second,
                            height = result.third,
                            fileSizeBytes = file.length(),
                            lastModified = file.lastModified(),
                            isLoading = false
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = "Không thể giải mã định dạng ảnh này"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Lỗi tải ảnh"
                    )
                }
            }
        }
    }

    private fun rotateRight() {
        updateState {
            copy(rotationDegrees = (rotationDegrees + 90f) % 360f)
        }
    }

    private fun resetTransform() {
        updateState {
            copy(scale = 1f, offsetX = 0f, offsetY = 0f)
        }
    }

    private fun updateTransform(scaleChange: Float, offsetChangeX: Float, offsetChangeY: Float) {
        updateState {
            val newScale = (scale * scaleChange).coerceIn(0.5f, 5.0f)
            copy(
                scale = newScale,
                offsetX = offsetX + offsetChangeX,
                offsetY = offsetY + offsetChangeY
            )
        }
    }

    private fun toggleInfoDialog() {
        updateState {
            copy(isInfoDialogVisible = !isInfoDialogVisible)
        }
    }
}
