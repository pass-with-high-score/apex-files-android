package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.StorageAnalysis
import app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository
import kotlinx.coroutines.flow.Flow

class AnalyzeStorageUseCase(
    private val cleanerRepository: CleanerRepository
) {
    operator fun invoke(): Flow<StorageAnalysis> {
        return cleanerRepository.analyzeStorage()
    }
}
