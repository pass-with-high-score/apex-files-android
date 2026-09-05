package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.JunkFile
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageAnalysis
import kotlinx.coroutines.flow.Flow

interface CleanerRepository {
    fun analyzeStorage(): Flow<StorageAnalysis>
    fun getLargeFiles(minSizeBytes: Long = 50 * 1024 * 1024L): Flow<List<FileItem>>
    fun getJunkFiles(): Flow<List<JunkFile>>
    suspend fun cleanJunkFiles(paths: List<String>): Result<Unit>
}
