package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class SetVaultPinUseCase(
    private val repository: VaultRepository
) {
    suspend fun setup(pin: String): Result<Unit> = repository.setupPin(pin)
    suspend fun change(oldPin: String, newPin: String): Result<Unit> = repository.changePin(oldPin, newPin)
}
