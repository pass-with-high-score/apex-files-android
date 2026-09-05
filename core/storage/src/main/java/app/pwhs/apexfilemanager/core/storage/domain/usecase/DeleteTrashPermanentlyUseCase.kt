package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository

class DeleteTrashPermanentlyUseCase(
    private val trashRepository: TrashRepository
) {
    suspend operator fun invoke(trashId: String): Result<Unit> {
        return trashRepository.deletePermanently(trashId)
    }
}
