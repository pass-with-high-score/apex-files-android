package app.pwhs.apexfilemanager.core.storage.data.network

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.ServerProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient
import java.io.File
import java.io.FileOutputStream

class FtpClient(private val server: NetworkServer) {

    private fun createClient(): FTPClient {
        return if (server.protocol == ServerProtocol.FTPS) {
            FTPSClient()
        } else {
            FTPClient()
        }.apply {
            connectTimeout = 8000
            defaultTimeout = 8000
        }
    }

    suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        val client = createClient()
        try {
            client.connect(server.host, server.port)
            val loginSuccess = if (server.isAnonymous) {
                client.login("anonymous", "")
            } else {
                client.login(server.username, server.password)
            }
            if (!loginSuccess) {
                return@withContext Result.failure(IllegalStateException("Đăng nhập FTP thất bại"))
            }
            client.enterLocalPassiveMode()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                if (client.isConnected) client.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun listFiles(path: String): Result<List<RemoteFileItem>> = withContext(Dispatchers.IO) {
        val client = createClient()
        try {
            client.connect(server.host, server.port)
            val loginSuccess = if (server.isAnonymous) {
                client.login("anonymous", "")
            } else {
                client.login(server.username, server.password)
            }
            if (!loginSuccess) {
                return@withContext Result.failure(IllegalStateException("Đăng nhập FTP thất bại"))
            }
            client.enterLocalPassiveMode()
            client.setFileType(FTP.BINARY_FILE_TYPE)

            val remotePath = path.ifBlank { "/" }
            val files = client.listFiles(remotePath) ?: emptyArray()

            val items = files.filter { it.name != "." && it.name != ".." }.map { file ->
                val fullPath = if (remotePath.endsWith("/")) {
                    "$remotePath${file.name}"
                } else {
                    "$remotePath/${file.name}"
                }
                RemoteFileItem(
                    name = file.name,
                    path = fullPath,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0L else file.size,
                    lastModified = file.timestamp?.timeInMillis ?: 0L
                )
            }.sortedWith(compareByDescending<RemoteFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                if (client.isConnected) client.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun downloadFile(remotePath: String, destFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        val client = createClient()
        try {
            client.connect(server.host, server.port)
            val loginSuccess = if (server.isAnonymous) {
                client.login("anonymous", "")
            } else {
                client.login(server.username, server.password)
            }
            if (!loginSuccess) {
                return@withContext Result.failure(IllegalStateException("Đăng nhập FTP thất bại"))
            }
            client.enterLocalPassiveMode()
            client.setFileType(FTP.BINARY_FILE_TYPE)

            FileOutputStream(destFile).use { fos ->
                val success = client.retrieveFile(remotePath, fos)
                if (!success) {
                    return@withContext Result.failure(IllegalStateException("Không thể tải tệp từ FTP"))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                if (client.isConnected) client.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}
