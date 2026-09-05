package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.ApkInfo
import app.pwhs.apexfilemanager.core.storage.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getApkInfo(path: String): Result<ApkInfo>
    fun getInstalledApps(includeSystem: Boolean = false): Flow<List<InstalledApp>>
    suspend fun backupApp(packageName: String): Result<String>
}
