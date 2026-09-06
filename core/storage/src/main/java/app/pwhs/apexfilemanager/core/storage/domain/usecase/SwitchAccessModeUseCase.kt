package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager
import app.pwhs.apexfilemanager.core.storage.domain.model.AccessMode

class SwitchAccessModeUseCase(
    private val privilegedManager: PrivilegedManager
) {
    suspend operator fun invoke(mode: AccessMode) {
        privilegedManager.switchMode(mode)
    }
}
