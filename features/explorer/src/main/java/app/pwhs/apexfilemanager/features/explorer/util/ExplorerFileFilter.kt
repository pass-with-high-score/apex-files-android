package app.pwhs.apexfilemanager.features.explorer.util

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.features.explorer.model.SortOption

object ExplorerFileFilter {

    fun isApkFile(name: String): Boolean = name.endsWith(".apk", ignoreCase = true)

    fun isArchiveFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "jar")
    }

    fun isImageFile(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("image/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    }

    fun isTextOrCodeFile(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("text/")) return true
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf(
            "txt", "json", "xml", "html", "htm", "css", "js", "ts", "kt", "java",
            "py", "c", "cpp", "h", "md", "log", "properties", "gradle", "sh", "yml",
            "yaml", "ini", "conf", "env", "sql", "csv"
        )
    }

    fun sortFiles(items: List<FileItem>, sortOption: SortOption): List<FileItem> {
        val folders = items.filter { it.isDirectory }
        val files = items.filter { !it.isDirectory }

        val comparator: Comparator<FileItem> = when (sortOption) {
            SortOption.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortOption.NAME_DESC -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortOption.DATE_DESC -> compareByDescending { it.modifiedTimestamp }
            SortOption.DATE_ASC -> compareBy { it.modifiedTimestamp }
            SortOption.SIZE_DESC -> compareByDescending { it.sizeBytes }
            SortOption.SIZE_ASC -> compareBy { it.sizeBytes }
        }

        return folders.sortedWith(comparator) + files.sortedWith(comparator)
    }

    fun buildBreadcrumbs(fullPath: String): List<app.pwhs.apexfilemanager.features.explorer.model.PathSegment> {
        val segments = mutableListOf<app.pwhs.apexfilemanager.features.explorer.model.PathSegment>()
        var current = java.io.File(fullPath)
        val pathList = mutableListOf<java.io.File>()

        while (current.parentFile != null) {
            pathList.add(0, current)
            current = current.parentFile ?: break
        }
        pathList.add(0, current)

        val rootExternal = android.os.Environment.getExternalStorageDirectory().absolutePath
        for (f in pathList) {
            val displayName = if (f.absolutePath == "/") "Gốc"
            else if (f.absolutePath == rootExternal) "Bộ nhớ"
            else f.name

            segments.add(app.pwhs.apexfilemanager.features.explorer.model.PathSegment(name = displayName, fullPath = f.absolutePath))
        }

        return segments
    }
}
