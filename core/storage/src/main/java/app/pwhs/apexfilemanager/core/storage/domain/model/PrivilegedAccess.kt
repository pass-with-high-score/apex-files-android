package app.pwhs.apexfilemanager.core.storage.domain.model

/**
 * Chế độ truy cập hệ thống tệp tin.
 */
enum class AccessMode {
    STANDARD,
    SHIZUKU,
    ROOT
}

/**
 * Trạng thái quyền đặc quyền hệ thống (Root & Shizuku).
 */
data class PrivilegedStatus(
    val isRootAvailable: Boolean = false,
    val isRootGranted: Boolean = false,
    val isShizukuAvailable: Boolean = false,
    val isShizukuGranted: Boolean = false,
    val activeMode: AccessMode = AccessMode.STANDARD
)

/**
 * Kết quả thực thi câu lệnh Shell đặc quyền.
 */
data class ShellResult(
    val code: Int,
    val output: List<String> = emptyList()
) {
    val isSuccess: Boolean
        get() = code == 0
}
