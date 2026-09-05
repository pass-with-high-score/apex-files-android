package app.pwhs.apexfilemanager.core.storage.data.network

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SmbClient(private val server: NetworkServer) {

    private fun getAuthContext(): AuthenticationContext {
        return if (server.isAnonymous || server.username.isBlank()) {
            AuthenticationContext.anonymous()
        } else {
            AuthenticationContext(server.username, server.password.toCharArray(), "")
        }
    }

    suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SMBClient()
        try {
            client.connect(server.host, server.port).use { connection ->
                val auth = getAuthContext()
                val session = connection.authenticate(auth)
                if (server.shareOrPath.isNotBlank()) {
                    (session.connectShare(server.shareOrPath) as? DiskShare)?.close()
                }
                session.close()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listFiles(path: String): Result<List<RemoteFileItem>> = withContext(Dispatchers.IO) {
        val client = SMBClient()
        try {
            client.connect(server.host, server.port).use { connection ->
                val auth = getAuthContext()
                val session = connection.authenticate(auth)
                val shareName = server.shareOrPath.ifBlank { "shared" }
                val share = session.connectShare(shareName) as DiskShare

                val folderPath = path.trimStart('/').replace('/', '\\')
                val list = share.list(folderPath)

                val items = list.filter { it.fileName != "." && it.fileName != ".." }.map { fileIdBoth ->
                    val isDir = (fileIdBoth.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) != 0L
                    val name = fileIdBoth.fileName
                    val fullPath = if (path.isBlank() || path == "/") "/$name" else "$path/$name"
                    RemoteFileItem(
                        name = name,
                        path = fullPath,
                        isDirectory = isDir,
                        size = if (isDir) 0L else fileIdBoth.endOfFile,
                        lastModified = fileIdBoth.changeTime.toEpochMillis()
                    )
                }.sortedWith(compareByDescending<RemoteFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })

                share.close()
                session.close()
                Result.success(items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(remotePath: String, destFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        val client = SMBClient()
        try {
            client.connect(server.host, server.port).use { connection ->
                val auth = getAuthContext()
                val session = connection.authenticate(auth)
                val shareName = server.shareOrPath.ifBlank { "shared" }
                val share = session.connectShare(shareName) as DiskShare

                val cleanPath = remotePath.trimStart('/').replace('/', '\\')
                val smbFile = share.openFile(
                    cleanPath,
                    setOf(com.hierynomus.msdtyp.AccessMask.GENERIC_READ),
                    null,
                    com.hierynomus.mssmb2.SMB2ShareAccess.ALL,
                    com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN,
                    null
                )

                smbFile.inputStream.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                smbFile.close()
                share.close()
                session.close()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
