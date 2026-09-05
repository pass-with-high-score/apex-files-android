package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository

class DeleteFilesUseCase(
    private val repository: FileRepository
) {
    suspend operator fun invoke(filePaths: List<String>): Result<Int> {
        return repository.deleteFiles(filePaths)
    }
}
