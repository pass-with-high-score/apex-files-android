package app.pwhs.apexfilemanager.core.storage.data.compat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume
import java.io.File

/**
 * Lớp tương thích và trừu tượng hóa việc truy xuất lưu trữ trên các phiên bản Android khác nhau.
 */
object StorageManagerCompat {

    /**
     * Kiểm tra xem ứng dụng đã được cấp quyền quản lý toàn bộ tệp (MANAGE_EXTERNAL_STORAGE) hay chưa.
     */
    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Các phiên bản cũ hơn dùng READ/WRITE_EXTERNAL_STORAGE qua runtime permission
        }
    }

    /**
     * Tạo Intent yêu cầu cấp quyền Tất cả tệp (All Files Access) cho ứng dụng trên Android 11+.
     */
    fun createManageAllFilesIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Quét và lấy danh sách các ổ đĩa bộ nhớ vật lý của thiết bị.
     */
    fun getStorageVolumes(context: Context): List<StorageVolume> {
        val volumes = mutableListOf<StorageVolume>()

        // Bộ nhớ trong chính (Internal Storage)
        val internalDir = Environment.getExternalStorageDirectory()
        if (internalDir != null && internalDir.exists()) {
            val stat = StatFs(internalDir.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize

            volumes.add(
                StorageVolume(
                    id = "primary",
                    name = "Bộ nhớ trong",
                    path = internalDir.absolutePath,
                    totalBytes = totalBytes,
                    freeBytes = freeBytes,
                    isRemovable = false,
                    isPrimary = true
                )
            )
        }

        return volumes
    }
}
