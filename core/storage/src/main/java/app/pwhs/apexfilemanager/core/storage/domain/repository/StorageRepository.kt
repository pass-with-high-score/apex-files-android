package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho việc truy xuất thông tin phân vùng bộ nhớ.
 */
interface StorageRepository {
    fun getStorageVolumes(): Flow<List<StorageVolume>>
}
