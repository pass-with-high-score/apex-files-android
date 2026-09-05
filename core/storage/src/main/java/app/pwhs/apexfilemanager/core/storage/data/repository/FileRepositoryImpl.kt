package app.pwhs.apexfilemanager.core.storage.data.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.URLConnection

/**
 * Triển khai đọc và duyệt tệp tin cục bộ thông qua File API.
 */
class FileRepositoryImpl : FileRepository {

    override fun getFilesInDirectory(directoryPath: String, showHidden: Boolean): Flow<List<FileItem>> = flow {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) {
            emit(emptyList())
            return@flow
        }

        val rawFiles = dir.listFiles() ?: emptyArray()
        val items = rawFiles
            .filter { file ->
                if (showHidden) true else !file.name.startsWith(".")
            }
            .map { file ->
                val isDir = file.isDirectory
                val mimeType = if (isDir) {
                    "resource/folder"
                } else {
                    URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
                }

                FileItem(
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
            .sortedWith(
                compareByDescending<FileItem> { it.isDirectory }
                    .thenBy { it.name.lowercase() }
            )

        emit(items)
    }.flowOn(Dispatchers.IO)
}
