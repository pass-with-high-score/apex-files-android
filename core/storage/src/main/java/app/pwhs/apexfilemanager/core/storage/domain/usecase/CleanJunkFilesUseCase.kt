package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository

class CleanJunkFilesUseCase(
    private val cleanerRepository: CleanerRepository
) {
    suspend operator fun invoke(paths: List<String>): Result<Unit> {
        return cleanerRepository.cleanJunkFiles(paths)
    }
}
