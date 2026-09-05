package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.ArchiveRepository

class ExtractArchiveUseCase(
    private val archiveRepository: ArchiveRepository
) {
    suspend operator fun invoke(
        zipFilePath: String,
        destDir: String,
        password: String? = null
    ): Result<Int> {
        return archiveRepository.extractArchive(zipFilePath, destDir, password)
    }
}
