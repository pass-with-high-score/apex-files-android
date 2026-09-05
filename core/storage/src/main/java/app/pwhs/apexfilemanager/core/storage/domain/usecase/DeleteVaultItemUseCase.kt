package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class DeleteVaultItemUseCase(
    private val repository: VaultRepository
) {
    suspend operator fun invoke(vaultItemId: String): Result<Unit> =
        repository.deleteVaultItem(vaultItemId)
}
