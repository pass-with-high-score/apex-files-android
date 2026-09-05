package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import app.pwhs.apexfilemanager.core.storage.data.network.FtpClient
import app.pwhs.apexfilemanager.core.storage.data.network.SftpClient
import app.pwhs.apexfilemanager.core.storage.data.network.SmbClient
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.ServerProtocol
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class NetworkServerRepositoryImpl(
    private val context: Context
) : NetworkServerRepository {

    private val serversFile by lazy {
        File(context.filesDir, "network_servers.json")
    }

    private val _serversFlow = MutableStateFlow<List<NetworkServer>>(emptyList())

    init {
        loadServers()
    }

    override fun getServers(): Flow<List<NetworkServer>> = _serversFlow.asStateFlow()

    override suspend fun getServerById(id: String): NetworkServer? {
        return _serversFlow.value.find { it.id == id }
    }

    override suspend fun saveServer(server: NetworkServer) = withContext(Dispatchers.IO) {
        val current = _serversFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == server.id }
        if (index >= 0) {
            current[index] = server
        } else {
            current.add(server)
        }
        persistServers(current)
        _serversFlow.value = current
    }

    override suspend fun deleteServer(id: String) = withContext(Dispatchers.IO) {
        val current = _serversFlow.value.filter { it.id != id }
        persistServers(current)
        _serversFlow.value = current
    }

    override suspend fun testConnection(server: NetworkServer): Result<Unit> {
        return when (server.protocol) {
            ServerProtocol.FTP, ServerProtocol.FTPS -> FtpClient(server).testConnection()
            ServerProtocol.SFTP -> SftpClient(server).testConnection()
            ServerProtocol.SMB -> SmbClient(server).testConnection()
        }
    }

    override suspend fun listFiles(server: NetworkServer, path: String): Result<List<RemoteFileItem>> {
        return when (server.protocol) {
            ServerProtocol.FTP, ServerProtocol.FTPS -> FtpClient(server).listFiles(path)
            ServerProtocol.SFTP -> SftpClient(server).listFiles(path)
            ServerProtocol.SMB -> SmbClient(server).listFiles(path)
        }
    }

    override suspend fun downloadFile(server: NetworkServer, remotePath: String, destFile: File): Result<Unit> {
        return when (server.protocol) {
            ServerProtocol.FTP, ServerProtocol.FTPS -> FtpClient(server).downloadFile(remotePath, destFile)
            ServerProtocol.SFTP -> SftpClient(server).downloadFile(remotePath, destFile)
            ServerProtocol.SMB -> SmbClient(server).downloadFile(remotePath, destFile)
        }
    }

    private fun loadServers() {
        try {
            if (!serversFile.exists()) return
            val jsonStr = serversFile.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<NetworkServer>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val protocolStr = obj.optString("protocol", ServerProtocol.SMB.name)
                val protocol = try {
                    ServerProtocol.valueOf(protocolStr)
                } catch (_: Exception) {
                    ServerProtocol.SMB
                }
                list.add(
                    NetworkServer(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        protocol = protocol,
                        host = obj.getString("host"),
                        port = obj.getInt("port"),
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        isAnonymous = obj.optBoolean("isAnonymous", false),
                        shareOrPath = obj.optString("shareOrPath", "")
                    )
                )
            }
            _serversFlow.value = list
        } catch (_: Exception) {
            _serversFlow.value = emptyList()
        }
    }

    private fun persistServers(list: List<NetworkServer>) {
        try {
            val array = JSONArray()
            list.forEach { server ->
                val obj = JSONObject().apply {
                    put("id", server.id)
                    put("name", server.name)
                    put("protocol", server.protocol.name)
                    put("host", server.host)
                    put("port", server.port)
                    put("username", server.username)
                    put("password", server.password)
                    put("isAnonymous", server.isAnonymous)
                    put("shareOrPath", server.shareOrPath)
                }
                array.put(obj)
            }
            serversFile.writeText(array.toString())
        } catch (_: Exception) {
        }
    }
}
