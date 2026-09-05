package app.pwhs.apexfilemanager.core.storage.domain.model

/**
 * Đại diện cho một tập tin hoặc thư mục trong hệ thống tệp.
 * Pure Kotlin, không dính Android SDK.
 */
data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long = 0L,
    val isDirectory: Boolean = false,
    val mimeType: String = "*/*",
    val modifiedTimestamp: Long = 0L,
    val isHidden: Boolean = false
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")
}
