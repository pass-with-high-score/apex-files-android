package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.ArchiveEntry
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem

interface ArchiveRepository {
    suspend fun listEntries(zipFilePath: String, password: String? = null): Result<List<ArchiveEntry>>
    suspend fun extractArchive(zipFilePath: String, destDir: String, password: String? = null): Result<Int>
    suspend fun createZip(sourcePaths: List<String>, destZipPath: String, password: String? = null): Result<FileItem>
}
