package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository
import kotlinx.coroutines.flow.Flow

class GetLargeFilesUseCase(
    private val cleanerRepository: CleanerRepository
) {
    operator fun invoke(minSizeBytes: Long = 50 * 1024 * 1024L): Flow<List<FileItem>> {
        return cleanerRepository.getLargeFiles(minSizeBytes)
    }
}
