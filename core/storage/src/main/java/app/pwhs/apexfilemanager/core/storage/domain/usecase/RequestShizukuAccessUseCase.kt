package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager

class RequestShizukuAccessUseCase(
    private val privilegedManager: PrivilegedManager
) {
    suspend operator fun invoke(): Boolean {
        return privilegedManager.requestShizuku()
    }
}
