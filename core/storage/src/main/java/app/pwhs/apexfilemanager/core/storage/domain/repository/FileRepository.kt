package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.ConflictStrategy
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho việc truy xuất và thao tác tệp tin trong hệ thống lưu trữ.
 */
interface FileRepository {
    fun getFilesInDirectory(directoryPath: String, showHidden: Boolean = false): Flow<List<FileItem>>
    fun getRecentFiles(limit: Int = 50): Flow<List<FileItem>>
    suspend fun createFolder(parentPath: String, folderName: String): Result<FileItem>
    suspend fun renameFile(filePath: String, newName: String): Result<FileItem>
    suspend fun deleteFiles(filePaths: List<String>): Result<Int>
    suspend fun copyFiles(sources: List<String>, targetDir: String, strategy: ConflictStrategy): Result<Int>
    suspend fun moveFiles(sources: List<String>, targetDir: String, strategy: ConflictStrategy): Result<Int>
    fun searchFiles(
        rootPath: String,
        query: String,
        category: app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory = app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory.ALL,
        showHidden: Boolean = false
    ): Flow<List<FileItem>>
}
