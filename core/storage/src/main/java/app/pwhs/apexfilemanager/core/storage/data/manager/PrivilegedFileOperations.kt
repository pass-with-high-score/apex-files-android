package app.pwhs.apexfilemanager.core.storage.data.manager

import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager
import app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.net.URLConnection

class PrivilegedFileOperations(
    private val privilegedManager: PrivilegedManager
) {

    suspend fun listDirectory(directoryPath: String, showHidden: Boolean = false): List<FileItem> {
        val currentMode = privilegedManager.status.value.activeMode

        // Thử dùng SuFile nếu đang ở chế độ ROOT
        if (currentMode == AccessMode.ROOT) {
            try {
                val suDir = SuFile(directoryPath)
                val files = suDir.listFiles()
                if (files != null) {
                    return files.filter { file ->
                        if (showHidden) true else !file.name.startsWith(".")
                    }.map { file ->
                        val mime = if (file.isDirectory) "vnd.android.document/directory"
                        else URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = if (file.isDirectory) 0L else file.length(),
                            isDirectory = file.isDirectory,
                            mimeType = mime,
                            modifiedTimestamp = file.lastModified(),
                            isHidden = file.name.startsWith(".")
                        )
                    }.sortedWith(
                        compareByDescending<FileItem> { it.isDirectory }
                            .thenBy { it.name.lowercase() }
                    )
                }
            } catch (_: Exception) { }
        }

        // Fallback: Chạy lệnh ls qua Root Shell hoặc Shizuku
        return listViaShellCommand(directoryPath, showHidden)
    }

    private suspend fun listViaShellCommand(dirPath: String, showHidden: Boolean): List<FileItem> {
        val safePath = dirPath.trimEnd('/')
        val cmd = if (showHidden) "ls -la '$safePath'" else "ls -l '$safePath'"
        val result = privilegedManager.executeCommand(cmd)
        if (!result.isSuccess) return emptyList()

        val items = mutableListOf<FileItem>()
        for (line in result.output) {
            val parts = line.trim().split(Regex("\\s+"))
            // Format tiêu chuẩn Linux ls -l:
            // drwxrwxr-x 3 root root 4096 2026-09-05 18:35 Alarms
            // -rw-rw-r-- 1 u0_a140 u0_a140 1234 2026-09-06 08:46 test.apk
            if (parts.size >= 8 && !parts[0].startsWith("total")) {
                val permissions = parts[0]
                val isDir = permissions.startsWith("d")
                val size = parts[4].toLongOrNull() ?: 0L
                val name = parts.subList(7, parts.size).joinToString(" ").substringBefore(" ->")
                if (name == "." || name == "..") continue
                if (!showHidden && name.startsWith(".")) continue

                val fullPath = if (safePath == "") "/$name" else "$safePath/$name"
                val mime = if (isDir) "vnd.android.document/directory"
                else URLConnection.guessContentTypeFromName(name) ?: "*/*"

                items.add(
                    FileItem(
                        id = fullPath,
                        name = name,
                        path = fullPath,
                        sizeBytes = if (isDir) 0L else size,
                        isDirectory = isDir,
                        mimeType = mime,
                        modifiedTimestamp = System.currentTimeMillis(),
                        isHidden = name.startsWith(".")
                    )
                )
            }
        }

        return items.sortedWith(
            compareByDescending<FileItem> { it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    suspend fun createFolder(parentPath: String, folderName: String): Boolean {
        val fullPath = "${parentPath.trimEnd('/')}/$folderName"
        val res = privilegedManager.executeCommand("mkdir -p '$fullPath'")
        return res.isSuccess
    }

    suspend fun deleteFileOrFolder(path: String): Boolean {
        val res = privilegedManager.executeCommand("rm -rf '$path'")
        return res.isSuccess
    }

    suspend fun rename(sourcePath: String, newName: String): Boolean {
        val parent = sourcePath.substringBeforeLast('/')
        val targetPath = if (parent.isEmpty()) "/$newName" else "$parent/$newName"
        val res = privilegedManager.executeCommand("mv '$sourcePath' '$targetPath'")
        return res.isSuccess
    }

    suspend fun copy(sourcePath: String, targetDir: String): Boolean {
        val res = privilegedManager.executeCommand("cp -rf '$sourcePath' '$targetDir/'")
        return res.isSuccess
    }

    suspend fun move(sourcePath: String, targetDir: String): Boolean {
        val res = privilegedManager.executeCommand("mv '$sourcePath' '$targetDir/'")
        return res.isSuccess
    }

    suspend fun readFile(path: String): String? {
        return privilegedManager.readFileText(path)
    }

    suspend fun writeFile(path: String, content: String): Boolean {
        return privilegedManager.writeFileText(path, content)
    }
}
