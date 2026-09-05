package app.pwhs.apexfilemanager.core.storage.domain.model

/**
 * Đại diện cho một ổ lưu trữ trong thiết bị (Bộ nhớ trong, Thẻ SD, USB OTG).
 * Pure Kotlin, không dính Android SDK.
 */
data class StorageVolume(
    val id: String,
    val name: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val isRemovable: Boolean = false,
    val isPrimary: Boolean = false
) {
    val usedBytes: Long
        get() = (totalBytes - freeBytes).coerceAtLeast(0L)

    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}
