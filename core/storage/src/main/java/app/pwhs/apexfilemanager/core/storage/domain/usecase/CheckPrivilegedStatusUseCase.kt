package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager
import app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus

class CheckPrivilegedStatusUseCase(
    private val privilegedManager: PrivilegedManager
) {
    suspend operator fun invoke(): PrivilegedStatus {
        return privilegedManager.checkStatus()
    }
}
