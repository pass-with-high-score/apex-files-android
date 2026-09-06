package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
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

class FileRepositoryImpl(
    private val context: Context,
    private val privilegedManager: app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager? = null
) : FileRepository {

    private val privilegedOps = privilegedManager?.let {
        app.pwhs.apexfilemanager.core.storage.data.manager.PrivilegedFileOperations(it)
    }

    override fun getFilesInDirectory(directoryPath: String, showHidden: Boolean): Flow<List<FileItem>> = flow {
        val dir = File(directoryPath)
        val rawFiles = if (dir.exists() && dir.isDirectory) dir.listFiles() else null

        if (rawFiles == null) {
            // Thử đặc quyền Root / Shizuku nếu thư mục bị hạn chế hoặc là phân vùng hệ thống
            if (privilegedOps != null) {
                val mode = privilegedManager?.status?.value?.activeMode
                if (mode != app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode.STANDARD || directoryPath == "/" || directoryPath.startsWith("/system") || directoryPath.startsWith("/data")) {
                    val privItems = privilegedOps.listDirectory(directoryPath, showHidden)
                    if (privItems.isNotEmpty() || directoryPath == "/" || directoryPath.startsWith("/system") || directoryPath.startsWith("/data") || directoryPath.contains("/Android/data")) {
                        emit(privItems)
                        return@flow
                    }
                }
            }

            if (!dir.exists()) {
                throw IllegalArgumentException("Thư mục không tồn tại: $directoryPath")
            }
            if (!dir.isDirectory) {
                throw IllegalArgumentException("Đường dẫn không phải thư mục: $directoryPath")
            }

            val path = dir.absolutePath
            if (path.contains("/Android/data") || path.contains("/Android/obb")) {
                throw SecurityException("Thư mục này bị giới hạn truy cập bởi Android. Hãy kích hoạt quyền Root hoặc Shizuku để mở khóa.")
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
        val results = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} != ${MediaStore.Files.FileColumns.MEDIA_TYPE_NONE} OR ${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT $limit"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext() && results.size < limit) {
                    val path = if (dataCol != -1) cursor.getString(dataCol) else null
                    if (path != null) {
                        val file = File(path)
                        if (file.exists() && file.isFile && !file.name.startsWith(".")) {
                            val name = if (nameCol != -1) cursor.getString(nameCol) ?: file.name else file.name
                            val size = if (sizeCol != -1) cursor.getLong(sizeCol) else file.length()
                            val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "*/*" else "*/*"
                            val modified = if (dateCol != -1) cursor.getLong(dateCol) * 1000L else file.lastModified()

                            results.add(
                                FileItem(
                                    id = path,
                                    name = name,
                                    path = path,
                                    sizeBytes = size,
                                    isDirectory = false,
                                    mimeType = mime,
                                    modifiedTimestamp = modified,
                                    isHidden = false
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // Fallback: If media store returns empty, scan common directories with depth 1
        if (results.isEmpty()) {
            val commonDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            )
            for (dir in commonDirs) {
                if (dir.exists() && dir.canRead()) {
                    dir.listFiles()?.filter { it.isFile && !it.name.startsWith(".") }?.forEach { f ->
                        results.add(mapToFileItem(f))
                    }
                }
            }
            results.sortByDescending { it.modifiedTimestamp }
            emit(results.take(limit))
        } else {
            emit(results)
        }
    }.flowOn(Dispatchers.IO)

    override fun getApkFiles(): Flow<List<FileItem>> = flow {
        val results = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE '%.apk' OR ${MediaStore.Files.FileColumns.MIME_TYPE} = 'application/vnd.android.package-archive'"
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = if (dataCol != -1) cursor.getString(dataCol) else null
                    if (path != null) {
                        val file = File(path)
                        if (file.exists() && file.isFile && file.name.endsWith(".apk", ignoreCase = true)) {
                            val name = if (nameCol != -1) cursor.getString(nameCol) ?: file.name else file.name
                            val size = if (sizeCol != -1) cursor.getLong(sizeCol) else file.length()
                            val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "application/vnd.android.package-archive" else "application/vnd.android.package-archive"
                            val modified = if (dateCol != -1) cursor.getLong(dateCol) * 1000L else file.lastModified()

                            results.add(
                                FileItem(
                                    id = path,
                                    name = name,
                                    path = path,
                                    sizeBytes = size,
                                    isDirectory = false,
                                    mimeType = mime,
                                    modifiedTimestamp = modified,
                                    isHidden = false
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // Fallback scan Download & Backup dirs if MediaStore empty
        if (results.isEmpty()) {
            val searchDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(Environment.getExternalStorageDirectory(), "ApexFileManager/Backup"),
                Environment.getExternalStorageDirectory()
            )
            for (dir in searchDirs) {
                if (dir.exists() && dir.canRead()) {
                    dir.listFiles()?.filter { it.isFile && it.name.endsWith(".apk", ignoreCase = true) }?.forEach { f ->
                        if (results.none { it.path == f.absolutePath }) {
                            results.add(mapToFileItem(f))
                        }
                    }
                }
            }
        }

        emit(results.sortedByDescending { it.modifiedTimestamp })
    }.flowOn(Dispatchers.IO)

    override suspend fun createFolder(parentPath: String, folderName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        runCatching {
            val newDir = File(parentPath, folderName)
            if (newDir.exists()) {
                throw IllegalStateException("Thư mục đã tồn tại")
            }
            if (!newDir.mkdirs()) {
                val success = privilegedOps?.createFolder(parentPath, folderName) ?: false
                if (!success) {
                    throw IllegalStateException("Không thể tạo thư mục tại: $parentPath")
                }
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
            val parent = source.parentFile
            val destination = File(parent, newName)
            if (destination.exists()) {
                throw IllegalStateException("Tên mới đã tồn tại: $newName")
            }
            val renamed = if (source.exists()) source.renameTo(destination) else false
            if (!renamed) {
                val privRenamed = privilegedOps?.rename(filePath, newName) ?: false
                if (!privRenamed) {
                    throw IllegalStateException("Đổi tên tệp thất bại")
                }
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
                if (file.exists() && file.deleteRecursively()) {
                    deletedCount++
                } else if (privilegedOps?.deleteFileOrFolder(path) == true) {
                    deletedCount++
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

    override suspend fun readFileText(filePath: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(filePath)
            if (file.exists() && file.canRead()) {
                file.readText()
            } else {
                privilegedOps?.readFile(filePath) ?: file.readText()
            }
        }
    }

    override suspend fun writeFileText(filePath: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                File(filePath).writeText(content)
            } catch (e: Exception) {
                val success = privilegedOps?.writeFile(filePath, content) ?: false
                if (!success) throw e
            }
        }
    }
}
