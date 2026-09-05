package app.pwhs.apexfilemanager.core.storage.data.repository

import android.os.Environment
import android.os.StatFs
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.JunkFile
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageAnalysis
import app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class CleanerRepositoryImpl : CleanerRepository {

    private val rootDir: File
        get() = Environment.getExternalStorageDirectory()

    override fun analyzeStorage(): Flow<StorageAnalysis> = flow {
        val root = rootDir
        val stat = StatFs(root.path)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = totalBytes - freeBytes

        var imgBytes = 0L
        var vidBytes = 0L
        var audBytes = 0L
        var docBytes = 0L
        var arcBytes = 0L
        var apkBytes = 0L
        var othBytes = 0L

        val imageExts = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
        val videoExts = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp")
        val audioExts = setOf("mp3", "m4a", "flac", "wav", "aac", "ogg")
        val docExts = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub")
        val arcExts = setOf("zip", "rar", "7z", "tar", "gz", "bz2")
        val apkExts = setOf("apk", "xapk", "apks")

        root.walkTopDown()
            .onEnter { !it.name.startsWith(".") || it.name == ".thumbnails" }
            .forEach { file ->
                if (file.isFile) {
                    val len = file.length()
                    val ext = file.extension.lowercase()
                    when {
                        ext in imageExts -> imgBytes += len
                        ext in videoExts -> vidBytes += len
                        ext in audioExts -> audBytes += len
                        ext in docExts -> docBytes += len
                        ext in arcExts -> arcBytes += len
                        ext in apkExts -> apkBytes += len
                        else -> othBytes += len
                    }
                }
            }

        emit(
            StorageAnalysis(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                imagesBytes = imgBytes,
                videosBytes = vidBytes,
                audioBytes = audBytes,
                documentsBytes = docBytes,
                archivesBytes = arcBytes,
                apksBytes = apkBytes,
                otherBytes = othBytes
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getLargeFiles(minSizeBytes: Long): Flow<List<FileItem>> = flow {
        val root = rootDir
        val list = mutableListOf<FileItem>()

        root.walkTopDown()
            .onEnter { !it.name.startsWith(".") }
            .forEach { file ->
                if (file.isFile && file.length() >= minSizeBytes) {
                    list.add(
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            isDirectory = false,
                            mimeType = "*/*",
                            modifiedTimestamp = file.lastModified(),
                            isHidden = file.name.startsWith(".")
                        )
                    )
                }
            }

        emit(list.sortedByDescending { it.sizeBytes }.take(100))
    }.flowOn(Dispatchers.IO)

    override fun getJunkFiles(): Flow<List<JunkFile>> = flow {
        val root = rootDir
        val list = mutableListOf<JunkFile>()
        val junkExts = setOf("tmp", "temp", "log", "bak")

        root.walkTopDown()
            .onEnter { it.name != ".apex_trash" }
            .forEach { file ->
                if (file.isFile) {
                    val ext = file.extension.lowercase()
                    val parentName = file.parentFile?.name?.lowercase() ?: ""
                    when {
                        ext in junkExts -> {
                            list.add(
                                JunkFile(
                                    path = file.absolutePath,
                                    name = file.name,
                                    sizeBytes = file.length(),
                                    reason = "Tệp tạm / nhật ký (.${ext})"
                                )
                            )
                        }
                        parentName == ".thumbnails" || parentName == "cache" -> {
                            list.add(
                                JunkFile(
                                    path = file.absolutePath,
                                    name = file.name,
                                    sizeBytes = file.length(),
                                    reason = "Bộ nhớ đệm hình thu nhỏ"
                                )
                            )
                        }
                        file.length() == 0L && ext.isNotEmpty() -> {
                            list.add(
                                JunkFile(
                                    path = file.absolutePath,
                                    name = file.name,
                                    sizeBytes = 0L,
                                    reason = "Tệp rỗng không có nội dung"
                                )
                            )
                        }
                    }
                }
            }

        emit(list.sortedByDescending { it.sizeBytes })
    }.flowOn(Dispatchers.IO)

    override suspend fun cleanJunkFiles(paths: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            for (path in paths) {
                val file = File(path)
                if (file.exists() && file.isFile) {
                    file.delete()
                }
            }
        }
    }
}
