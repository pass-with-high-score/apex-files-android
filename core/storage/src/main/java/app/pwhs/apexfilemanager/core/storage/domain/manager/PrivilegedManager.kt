package app.pwhs.apexfilemanager.core.storage.domain.manager

import app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode
import app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus
import app.pwhs.apexfilemanager.core.storage.domain.model.ShellResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface quản lý quyền đặc quyền (Root & Shizuku).
 * Pure Kotlin, Clean Architecture.
 */
interface PrivilegedManager {
    val status: StateFlow<PrivilegedStatus>

    suspend fun checkStatus(): PrivilegedStatus

    suspend fun requestRoot(): Boolean

    suspend fun requestShizuku(): Boolean

    suspend fun switchMode(mode: AccessMode)

    suspend fun executeCommand(command: String): ShellResult

    suspend fun readFileText(filePath: String): String?

    suspend fun writeFileText(filePath: String, content: String): Boolean
}
