package app.pwhs.apexfilemanager.core.storage.data.manager

import android.content.Context
import android.content.pm.PackageManager
import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager
import app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode
import app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus
import app.pwhs.apexfilemanager.core.storage.domain.model.ShellResult
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

class PrivilegedManagerImpl(
    private val context: Context
) : PrivilegedManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _status = MutableStateFlow(PrivilegedStatus())
    override val status: StateFlow<PrivilegedStatus> = _status.asStateFlow()

    private var permissionDeferred: CompletableDeferred<Boolean>? = null

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        permissionDeferred?.complete(granted)
        permissionDeferred = null
        _status.update {
            val newMode = if (granted && it.activeMode == AccessMode.STANDARD) AccessMode.SHIZUKU else it.activeMode
            it.copy(isShizukuGranted = granted, activeMode = newMode)
        }
    }

    init {
        try {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        } catch (_: Throwable) { }

        scope.launch {
            checkStatus()
        }
    }

    override suspend fun checkStatus(): PrivilegedStatus = withContext(Dispatchers.IO) {
        val isRootAvail = checkRootBinaryAvailable()
        val isRootGranted = checkRootGranted()
        val isShizukuAvail = checkShizukuAvailable()
        val isShizukuGranted = checkShizukuGranted()

        val activeMode = when {
            isRootGranted -> AccessMode.ROOT
            isShizukuGranted -> AccessMode.SHIZUKU
            else -> AccessMode.STANDARD
        }

        val newStatus = PrivilegedStatus(
            isRootAvailable = isRootAvail,
            isRootGranted = isRootGranted,
            isShizukuAvailable = isShizukuAvail,
            isShizukuGranted = isShizukuGranted,
            activeMode = activeMode
        )
        _status.value = newStatus
        newStatus
    }

    override suspend fun requestRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            val shell = Shell.getShell()
            val granted = shell.isRoot
            _status.update {
                it.copy(
                    isRootAvailable = true,
                    isRootGranted = granted,
                    activeMode = if (granted) AccessMode.ROOT else it.activeMode
                )
            }
            granted
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun requestShizuku(): Boolean = withContext(Dispatchers.Main) {
        if (!checkShizukuAvailable()) return@withContext false
        if (checkShizukuGranted()) {
            _status.update { it.copy(isShizukuGranted = true, activeMode = AccessMode.SHIZUKU) }
            return@withContext true
        }

        val deferred = CompletableDeferred<Boolean>()
        permissionDeferred = deferred
        try {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
            deferred.await()
        } catch (e: Exception) {
            permissionDeferred = null
            false
        }
    }

    override suspend fun switchMode(mode: AccessMode) {
        _status.update { it.copy(activeMode = mode) }
    }

    override suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        val currentMode = _status.value.activeMode
        if (currentMode == AccessMode.ROOT) {
            try {
                val res = Shell.cmd(command).exec()
                return@withContext ShellResult(code = res.code, output = res.out)
            } catch (e: Exception) {
                return@withContext ShellResult(code = -1, output = listOf(e.localizedMessage ?: "Root execution error"))
            }
        } else if (currentMode == AccessMode.SHIZUKU && checkShizukuGranted()) {
            try {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }
                val proc = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
                val lines = proc.inputStream.bufferedReader().readLines()
                val exitCode = proc.waitFor()
                return@withContext ShellResult(code = exitCode, output = lines)
            } catch (e: Exception) {
                return@withContext ShellResult(code = -1, output = listOf(e.localizedMessage ?: "Shizuku execution error"))
            }
        }

        // Standard shell execution
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val lines = proc.inputStream.bufferedReader().readLines()
            val exitCode = proc.waitFor()
            ShellResult(code = exitCode, output = lines)
        } catch (e: Exception) {
            ShellResult(code = -1, output = listOf(e.localizedMessage ?: "Standard execution error"))
        }
    }

    override suspend fun readFileText(filePath: String): String? = withContext(Dispatchers.IO) {
        val currentMode = _status.value.activeMode
        if (currentMode == AccessMode.ROOT) {
            try {
                val suFile = SuFile(filePath)
                if (suFile.exists() && suFile.canRead()) {
                    SuFileInputStream.open(suFile).bufferedReader().use { return@withContext it.readText() }
                }
            } catch (_: Exception) { }
            // Fallback root cat
            val res = Shell.cmd("cat '$filePath'").exec()
            if (res.isSuccess) {
                return@withContext res.out.joinToString("\n")
            }
        } else if (currentMode == AccessMode.SHIZUKU && checkShizukuGranted()) {
            val res = executeCommand("cat '$filePath'")
            if (res.isSuccess) {
                return@withContext res.output.joinToString("\n")
            }
        }

        // Standard file read
        try {
            val file = File(filePath)
            if (file.exists() && file.canRead()) {
                file.readText()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun writeFileText(filePath: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val currentMode = _status.value.activeMode
        if (currentMode == AccessMode.ROOT) {
            try {
                val suFile = SuFile(filePath)
                SuFileOutputStream.open(suFile).bufferedWriter().use {
                    it.write(content)
                    return@withContext true
                }
            } catch (_: Exception) { }
            // Fallback echo/cat via root shell
            val escaped = content.replace("'", "'\\''")
            val res = Shell.cmd("echo '$escaped' > '$filePath'").exec()
            return@withContext res.isSuccess
        } else if (currentMode == AccessMode.SHIZUKU && checkShizukuGranted()) {
            val escaped = content.replace("'", "'\\''")
            val res = executeCommand("echo '$escaped' > '$filePath'")
            return@withContext res.isSuccess
        }

        // Standard file write
        try {
            val file = File(filePath)
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkRootBinaryAvailable(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return try {
            Runtime.getRuntime().exec(arrayOf("which", "su")).waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    private fun checkRootGranted(): Boolean {
        return try {
            Shell.isAppGrantedRoot() == true || (Shell.getCachedShell()?.isRoot == true)
        } catch (_: Exception) {
            false
        }
    }

    private fun checkShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    private fun checkShizukuGranted(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        private const val SHIZUKU_REQUEST_CODE = 1002
    }
}
