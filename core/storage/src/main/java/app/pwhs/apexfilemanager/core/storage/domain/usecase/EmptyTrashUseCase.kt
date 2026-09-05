package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository

class EmptyTrashUseCase(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return trashRepository.emptyTrash()
    }
}
