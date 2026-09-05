package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.ConflictStrategy
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository

class MoveFilesUseCase(
    private val repository: FileRepository
) {
    suspend operator fun invoke(
        sources: List<String>,
        targetDir: String,
        strategy: ConflictStrategy = ConflictStrategy.AUTO_RENAME
    ): Result<Int> {
        return repository.moveFiles(sources, targetDir, strategy)
    }
}
