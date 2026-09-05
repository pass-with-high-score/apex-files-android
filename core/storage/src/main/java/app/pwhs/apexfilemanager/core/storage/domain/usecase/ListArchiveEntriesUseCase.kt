package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.ArchiveEntry
import app.pwhs.apexfilemanager.core.storage.domain.repository.ArchiveRepository

class ListArchiveEntriesUseCase(
    private val archiveRepository: ArchiveRepository
) {
    suspend operator fun invoke(zipFilePath: String, password: String? = null): Result<List<ArchiveEntry>> {
        return archiveRepository.listEntries(zipFilePath, password)
    }
}
