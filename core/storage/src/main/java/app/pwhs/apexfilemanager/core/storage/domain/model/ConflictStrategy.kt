package app.pwhs.apexfilemanager.core.storage.domain.model

/**
 * Chiến lược giải quyết xung đột khi sao chép/di chuyển file trùng tên.
 */
enum class ConflictStrategy {
    OVERWRITE,   // Ghi đè file đích
    SKIP,        // Bỏ qua không sao chép file trùng
    AUTO_RENAME  // Tự động đổi tên thêm hậu tố (1), (2)...
}
