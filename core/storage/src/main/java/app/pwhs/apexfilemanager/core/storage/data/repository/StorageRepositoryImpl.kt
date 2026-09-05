package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import app.pwhs.apexfilemanager.core.storage.data.compat.StorageManagerCompat
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume
import app.pwhs.apexfilemanager.core.storage.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Triển khai truy xuất thông tin phân vùng bộ nhớ thực tế.
 */
class StorageRepositoryImpl(
    private val context: Context
) : StorageRepository {

    override fun getStorageVolumes(): Flow<List<StorageVolume>> = flow {
        val volumes = StorageManagerCompat.getStorageVolumes(context)
        emit(volumes)
    }.flowOn(Dispatchers.IO)
}
