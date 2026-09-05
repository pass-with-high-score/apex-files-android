package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository

class MoveToTrashUseCase(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(paths: List<String>): Result<Unit> {
        return trashRepository.moveToTrash(paths)
    }
}
