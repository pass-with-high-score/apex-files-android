package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume
import app.pwhs.apexfilemanager.core.storage.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase lấy danh sách các phân vùng bộ nhớ (Bộ nhớ trong, thẻ nhớ ngoài...).
 */
class GetStorageVolumesUseCase(
    private val repository: StorageRepository
) {
    operator fun invoke(): Flow<List<StorageVolume>> {
        return repository.getStorageVolumes()
    }
}
