package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import kotlinx.coroutines.flow.Flow
import java.io.File

interface NetworkServerRepository {
    fun getServers(): Flow<List<NetworkServer>>
    suspend fun getServerById(id: String): NetworkServer?
    suspend fun saveServer(server: NetworkServer)
    suspend fun deleteServer(id: String)
    suspend fun testConnection(server: NetworkServer): Result<Unit>
    suspend fun listFiles(server: NetworkServer, path: String): Result<List<RemoteFileItem>>
    suspend fun downloadFile(server: NetworkServer, remotePath: String, destFile: File): Result<Unit>
}
