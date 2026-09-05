package app.pwhs.apexfilemanager.core.storage.data.repository

import app.pwhs.apexfilemanager.core.storage.data.compat.StorageManagerCompat
import app.pwhs.apexfilemanager.core.storage.domain.model.ConflictStrategy
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import app.pwhs.apexfilemanager.core.storage.util.VietnameseNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLConnection

class FileRepositoryImpl : FileRepository {

    override fun getFilesInDirectory(directoryPath: String, showHidden: Boolean): Flow<List<FileItem>> = flow {
        val dir = File(directoryPath)
        if (!dir.exists()) {
            throw IllegalArgumentException("Thư mục không tồn tại: $directoryPath")
        }
        if (!dir.isDirectory) {
            throw IllegalArgumentException("Đường dẫn không phải thư mục: $directoryPath")
        }

        val rawFiles = dir.listFiles()
        if (rawFiles == null) {
            val path = dir.absolutePath
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                throw SecurityException("Thư mục này bị giới hạn truy cập bởi chính sách bảo mật của hệ điều hành Android.")
            }
            if (!StorageManagerCompat.hasAllFilesAccess()) {
                throw SecurityException("Ứng dụng chưa được cấp quyền quản lý tất cả tệp tin.")
            }
            throw SecurityException("Không có quyền đọc thư mục này: $directoryPath")
        }

        val items = rawFiles
            .filter { file ->
                if (showHidden) true else !file.name.startsWith(".")
            }
            .map { file -> mapToFileItem(file) }
            .sortedWith(
                compareByDescending<FileItem> { it.isDirectory }
                    .thenBy { it.name.lowercase() }
            )

        emit(items)
    }.flowOn(Dispatchers.IO)

    override fun getRecentFiles(limit: Int): Flow<List<FileItem>> = flow {
        val rootDir = android.os.Environment.getExternalStorageDirectory()
        if (!rootDir.exists() || !rootDir.canRead()) {
            emit(emptyList())
            return@flow
        }

        val results = mutableListOf<FileItem>()
        try {
            rootDir.walkTopDown()
                .onEnter { dir ->
                    val abs = dir.absolutePath
                    !abs.contains("/Android/data") && !abs.contains("/Android/obb")
                }
                .filter { it.isFile && !it.name.startsWith(".") }
                .forEach { file ->
                    results.add(mapToFileItem(file))
                }
        } catch (_: Exception) { }

        val recent = results
            .sortedByDescending { it.modifiedTimestamp }
            .take(limit)
        emit(recent)
    }.flowOn(Dispatchers.IO)

    override suspend fun createFolder(parentPath: String, folderName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        runCatching {
            val newDir = File(parentPath, folderName)
            if (newDir.exists()) {
                throw IllegalStateException("Thư mục đã tồn tại")
            }
            if (!newDir.mkdirs()) {
                throw IllegalStateException("Không thể tạo thư mục tại: $parentPath")
            }
            FileItem(
                id = newDir.absolutePath,
                name = newDir.name,
                path = newDir.absolutePath,
                sizeBytes = 0L,
                isDirectory = true,
                mimeType = "resource/folder",
                modifiedTimestamp = newDir.lastModified(),
                isHidden = newDir.name.startsWith(".")
            )
        }
    }

    override suspend fun renameFile(filePath: String, newName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        runCatching {
            val source = File(filePath)
            if (!source.exists()) {
                throw IllegalArgumentException("Tệp tin không tồn tại: $filePath")
            }
            val destination = File(source.parentFile, newName)
            if (destination.exists()) {
                throw IllegalStateException("Tên mới đã tồn tại: $newName")
            }
            if (!source.renameTo(destination)) {
                throw IllegalStateException("Đổi tên tệp thất bại")
            }
            val isDir = destination.isDirectory
            FileItem(
                id = destination.absolutePath,
                name = destination.name,
                path = destination.absolutePath,
                sizeBytes = if (isDir) 0L else destination.length(),
                isDirectory = isDir,
                mimeType = if (isDir) "resource/folder" else URLConnection.guessContentTypeFromName(destination.name) ?: "*/*",
                modifiedTimestamp = destination.lastModified(),
                isHidden = destination.name.startsWith(".")
            )
        }
    }

    override suspend fun deleteFiles(filePaths: List<String>): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            var deletedCount = 0
            for (path in filePaths) {
                val file = File(path)
                if (file.exists()) {
                    if (file.deleteRecursively()) {
                        deletedCount++
                    }
                }
            }
            deletedCount
        }
    }

    override suspend fun copyFiles(
        sources: List<String>,
        targetDir: String,
        strategy: ConflictStrategy
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFolder = File(targetDir)
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                throw IllegalStateException("Thư mục đích không tồn tại: $targetDir")
            }

            var copiedCount = 0
            for (sourcePath in sources) {
                val srcFile = File(sourcePath)
                if (!srcFile.exists()) continue

                val destFile = resolveDestinationFile(targetFolder, srcFile.name, strategy) ?: continue
                if (srcFile.copyRecursively(destFile, overwrite = (strategy == ConflictStrategy.OVERWRITE))) {
                    copiedCount++
                }
            }
            copiedCount
        }
    }

    override suspend fun moveFiles(
        sources: List<String>,
        targetDir: String,
        strategy: ConflictStrategy
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val targetFolder = File(targetDir)
            if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                throw IllegalStateException("Thư mục đích không tồn tại: $targetDir")
            }

            var movedCount = 0
            for (sourcePath in sources) {
                val srcFile = File(sourcePath)
                if (!srcFile.exists()) continue

                val destFile = resolveDestinationFile(targetFolder, srcFile.name, strategy) ?: continue
                if (srcFile.renameTo(destFile)) {
                    movedCount++
                } else {
                    // Fallback: Copy rồi xóa file nguồn nếu thành công toàn vẹn
                    if (srcFile.copyRecursively(destFile, overwrite = (strategy == ConflictStrategy.OVERWRITE))) {
                        srcFile.deleteRecursively()
                        movedCount++
                    }
                }
            }
            movedCount
        }
    }

    override fun searchFiles(
        rootPath: String,
        query: String,
        category: SearchCategory,
        showHidden: Boolean
    ): Flow<List<FileItem>> = flow {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val rootDir = File(rootPath)
        if (!rootDir.exists() || !rootDir.canRead()) {
            emit(emptyList())
            return@flow
        }

        val results = mutableListOf<FileItem>()
        var lastEmitTime = System.currentTimeMillis()

        try {
            rootDir.walkTopDown()
                .onEnter { dir ->
                    val name = dir.name
                    if (!showHidden && name.startsWith(".")) return@onEnter false
                    val abs = dir.absolutePath
                    if (abs.contains("/Android/data") || abs.contains("/Android/obb")) return@onEnter false
                    true
                }
                .forEach { file ->
                    if (file.absolutePath == rootDir.absolutePath) return@forEach
                    if (!showHidden && file.name.startsWith(".")) return@forEach

                    val matchesQuery = app.pwhs.apexfilemanager.core.storage.util.VietnameseNormalizer
                        .containsIgnoreCaseAndAccents(file.name, trimmedQuery)

                    if (matchesQuery) {
                        val ext = file.extension
                        if (category.matches(ext)) {
                            results.add(mapToFileItem(file))
                            val now = System.currentTimeMillis()
                            // Emit incremental results every 300ms or when reaching batch size to keep UI responsive
                            if (results.size % 25 == 0 || (now - lastEmitTime) > 300) {
                                emit(results.toList())
                                lastEmitTime = now
                            }
                        }
                    }
                }
        } catch (_: Exception) {
            // Gracefully handle any permission or I/O interrupts
        }

        emit(results.toList())
    }.flowOn(Dispatchers.IO)

    private fun resolveDestinationFile(targetFolder: File, originalName: String, strategy: ConflictStrategy): File? {
        val destFile = File(targetFolder, originalName)
        if (!destFile.exists()) {
            return destFile
        }

        return when (strategy) {
            ConflictStrategy.OVERWRITE -> destFile
            ConflictStrategy.SKIP -> null
            ConflictStrategy.AUTO_RENAME -> {
                val nameWithoutExt = originalName.substringBeforeLast('.')
                val ext = if (originalName.contains('.')) ".${originalName.substringAfterLast('.')}" else ""
                var counter = 1
                var newFile: File
                do {
                    newFile = File(targetFolder, "$nameWithoutExt ($counter)$ext")
                    counter++
                } while (newFile.exists())
                newFile
            }
        }
    }

    private fun mapToFileItem(file: File): FileItem {
        val isDir = file.isDirectory
        val mimeType = if (isDir) {
            "resource/folder"
        } else {
            URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
        }

        return FileItem(
            id = file.absolutePath,
            name = file.name,
            path = file.absolutePath,
            sizeBytes = if (isDir) 0L else file.length(),
            isDirectory = isDir,
            mimeType = mimeType,
            modifiedTimestamp = file.lastModified(),
            isHidden = file.name.startsWith(".")
        )
    }
}
