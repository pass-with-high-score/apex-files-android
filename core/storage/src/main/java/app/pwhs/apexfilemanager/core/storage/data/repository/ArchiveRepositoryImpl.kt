package app.pwhs.apexfilemanager.core.storage.data.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.ArchiveEntry
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.ArchiveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.net.URLConnection

class ArchiveRepositoryImpl : ArchiveRepository {

    override suspend fun listEntries(
        zipFilePath: String,
        password: String?
    ): Result<List<ArchiveEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(zipFilePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Tệp lưu trữ không tồn tại: $zipFilePath")
            }

            val zipFile = if (password.isNullOrEmpty()) {
                ZipFile(file)
            } else {
                ZipFile(file, password.toCharArray())
            }

            val headers = zipFile.fileHeaders
            headers.map { header ->
                val fullPath = header.fileName.trimEnd('/')
                val name = fullPath.substringAfterLast('/')
                ArchiveEntry(
                    name = name.ifEmpty { fullPath },
                    path = header.fileName,
                    isDirectory = header.isDirectory,
                    uncompressedSize = header.uncompressedSize,
                    compressedSize = header.compressedSize,
                    modifiedTimestamp = header.lastModifiedTime
                )
            }
        }
    }

    override suspend fun extractArchive(
        zipFilePath: String,
        destDir: String,
        password: String?
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(zipFilePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Tệp lưu trữ không tồn tại: $zipFilePath")
            }

            val targetDir = File(destDir)
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IllegalStateException("Không thể tạo thư mục giải nén: $destDir")
            }

            val zipFile = if (password.isNullOrEmpty()) {
                ZipFile(file)
            } else {
                ZipFile(file, password.toCharArray())
            }

            val entryCount = zipFile.fileHeaders.size
            zipFile.extractAll(destDir)
            entryCount
        }
    }

    override suspend fun createZip(
        sourcePaths: List<String>,
        destZipPath: String,
        password: String?
    ): Result<FileItem> = withContext(Dispatchers.IO) {
        runCatching {
            val destFile = File(destZipPath)
            destFile.parentFile?.let { parent ->
                if (!parent.exists()) parent.mkdirs()
            }

            val zipFile = if (password.isNullOrEmpty()) {
                ZipFile(destFile)
            } else {
                ZipFile(destFile, password.toCharArray())
            }

            val zipParameters = ZipParameters().apply {
                compressionMethod = CompressionMethod.DEFLATE
                compressionLevel = CompressionLevel.NORMAL
                if (!password.isNullOrEmpty()) {
                    isEncryptFiles = true
                    encryptionMethod = EncryptionMethod.AES
                    aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                }
            }

            for (path in sourcePaths) {
                val src = File(path)
                if (!src.exists()) continue
                if (src.isDirectory) {
                    zipFile.addFolder(src, zipParameters)
                } else {
                    zipFile.addFile(src, zipParameters)
                }
            }

            FileItem(
                id = destFile.absolutePath,
                name = destFile.name,
                path = destFile.absolutePath,
                sizeBytes = destFile.length(),
                isDirectory = false,
                mimeType = "application/zip",
                modifiedTimestamp = destFile.lastModified(),
                isHidden = destFile.name.startsWith(".")
            )
        }
    }
}
