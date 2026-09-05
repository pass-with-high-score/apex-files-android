package app.pwhs.apexfilemanager.core.storage.domain.model

enum class SearchCategory {
    ALL,
    DOCUMENT,
    IMAGE,
    VIDEO,
    AUDIO,
    ARCHIVE,
    APK;

    fun matches(extension: String): Boolean {
        val ext = extension.lowercase()
        return when (this) {
            ALL -> true
            DOCUMENT -> ext in setOf("doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf", "odt", "epub")
            IMAGE -> ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")
            VIDEO -> ext in setOf("mp4", "mkv", "avi", "mov", "flv", "wmv", "webm", "3gp", "m4v")
            AUDIO -> ext in setOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma")
            ARCHIVE -> ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
            APK -> ext in setOf("apk", "xapk", "apks")
        }
    }
}
