package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.TrashItem
import kotlinx.coroutines.flow.Flow

interface TrashRepository {
    fun getTrashItems(): Flow<List<TrashItem>>
    suspend fun moveToTrash(paths: List<String>): Result<Unit>
    suspend fun restoreItem(trashId: String): Result<Unit>
    suspend fun deletePermanently(trashId: String): Result<Unit>
    suspend fun emptyTrash(): Result<Unit>
}
