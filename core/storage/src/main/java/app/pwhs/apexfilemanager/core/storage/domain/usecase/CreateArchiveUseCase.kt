package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.ArchiveRepository

class CreateArchiveUseCase(
    private val archiveRepository: ArchiveRepository
) {
    suspend operator fun invoke(
        sourcePaths: List<String>,
        destZipPath: String,
        password: String? = null
    ): Result<FileItem> {
        return archiveRepository.createZip(sourcePaths, destZipPath, password)
    }
}
