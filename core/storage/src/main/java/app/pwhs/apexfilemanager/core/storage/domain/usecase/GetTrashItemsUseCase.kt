package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.TrashItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository
import kotlinx.coroutines.flow.Flow

class GetTrashItemsUseCase(
    private val trashRepository: TrashRepository
) {
    operator fun invoke(): Flow<List<TrashItem>> {
        return trashRepository.getTrashItems()
    }
}
