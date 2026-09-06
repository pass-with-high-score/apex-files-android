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
    private val context: Context,
    private val privilegedManager: app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager? = null
) : StorageRepository {

    override fun getStorageVolumes(): Flow<List<StorageVolume>> = flow {
        val volumes = StorageManagerCompat.getStorageVolumes(context).toMutableList()
        val isRoot = privilegedManager?.status?.value?.isRootGranted == true
        if (isRoot) {
            val rootDir = java.io.File("/")
            val total = rootDir.totalSpace
            val free = rootDir.freeSpace
            volumes.add(
                StorageVolume(
                    id = "root_system",
                    name = "Hệ thống (Root)",
                    path = "/",
                    totalBytes = if (total > 0) total else 10L * 1024 * 1024 * 1024,
                    freeBytes = if (free > 0) free else 2L * 1024 * 1024 * 1024,
                    isRemovable = false,
                    isPrimary = false
                )
            )
        }
        emit(volumes)
    }.flowOn(Dispatchers.IO)
}
