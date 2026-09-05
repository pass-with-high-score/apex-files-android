package app.pwhs.apexfilemanager.core.storage.data.network

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Vector

class SftpClient(private val server: NetworkServer) {

    private fun openSession(): Pair<Session, ChannelSftp> {
        val jsch = JSch()
        val session = jsch.getSession(server.username, server.host, server.port).apply {
            setPassword(server.password)
            setConfig("StrictHostKeyChecking", "no")
            timeout = 8000
            connect(8000)
        }
        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect(8000)
        return Pair(session, channel)
    }

    suspend fun testConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        var session: Session? = null
        var channel: ChannelSftp? = null
        try {
            val pair = openSession()
            session = pair.first
            channel = pair.second
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                channel?.disconnect()
                session?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun listFiles(path: String): Result<List<RemoteFileItem>> = withContext(Dispatchers.IO) {
        var session: Session? = null
        var channel: ChannelSftp? = null
        try {
            val pair = openSession()
            session = pair.first
            channel = pair.second

            val remotePath = path.ifBlank { "." }
            @Suppress("UNCHECKED_CAST")
            val vector = channel.ls(remotePath) as Vector<ChannelSftp.LsEntry>

            val items = vector.filter { it.filename != "." && it.filename != ".." }.map { entry ->
                val fullPath = if (remotePath == "." || remotePath == "/") {
                    "/${entry.filename}"
                } else if (remotePath.endsWith("/")) {
                    "$remotePath${entry.filename}"
                } else {
                    "$remotePath/${entry.filename}"
                }
                val isDir = entry.attrs.isDir
                RemoteFileItem(
                    name = entry.filename,
                    path = fullPath,
                    isDirectory = isDir,
                    size = if (isDir) 0L else entry.attrs.size,
                    lastModified = entry.attrs.mTime * 1000L
                )
            }.sortedWith(compareByDescending<RemoteFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                channel?.disconnect()
                session?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun downloadFile(remotePath: String, destFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        var session: Session? = null
        var channel: ChannelSftp? = null
        try {
            val pair = openSession()
            session = pair.first
            channel = pair.second

            FileOutputStream(destFile).use { fos ->
                channel.get(remotePath, fos)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                channel?.disconnect()
                session?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}
