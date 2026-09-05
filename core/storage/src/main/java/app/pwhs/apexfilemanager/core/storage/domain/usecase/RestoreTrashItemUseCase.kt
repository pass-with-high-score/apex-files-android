package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository

class RestoreTrashItemUseCase(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(trashId: String): Result<Unit> {
        return trashRepository.restoreItem(trashId)
    }
}
