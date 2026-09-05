package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository

class CreateFolderUseCase(
    private val repository: FileRepository
) {
    suspend operator fun invoke(parentPath: String, folderName: String): Result<FileItem> {
        return repository.createFolder(parentPath, folderName)
    }
}
