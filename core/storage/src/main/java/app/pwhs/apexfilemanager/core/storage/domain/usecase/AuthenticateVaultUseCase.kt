package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class AuthenticateVaultUseCase(
    private val repository: VaultRepository
) {
    suspend fun verifyPin(pin: String): Boolean = repository.verifyPin(pin)
    suspend fun isBiometricEnabled(): Boolean = repository.isBiometricEnabled()
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = repository.setBiometricEnabled(enabled)
}
