package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager
import app.pwhs.apexfilemanager.core.storage.domain.model.PrivilegedStatus
import kotlinx.coroutines.flow.StateFlow

class GetPrivilegedStatusUseCase(
    private val privilegedManager: PrivilegedManager
) {
    operator fun invoke(): StateFlow<PrivilegedStatus> {
        return privilegedManager.status
    }
}
