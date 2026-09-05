package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class CheckVaultSetupUseCase(
    private val repository: VaultRepository
) {
    suspend operator fun invoke(): Boolean = repository.isVaultSetUp()
}
