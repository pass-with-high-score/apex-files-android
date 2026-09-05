package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository

class RenameFileUseCase(
    private val repository: FileRepository
) {
    suspend operator fun invoke(filePath: String, newName: String): Result<FileItem> {
        return repository.renameFile(filePath, newName)
    }
}
