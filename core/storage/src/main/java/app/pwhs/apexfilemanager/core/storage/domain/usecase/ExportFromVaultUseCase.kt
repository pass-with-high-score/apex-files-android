package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class ExportFromVaultUseCase(
    private val repository: VaultRepository
) {
    suspend operator fun invoke(vaultItemId: String, destinationDir: String? = null): Result<String> =
        repository.exportFile(vaultItemId, destinationDir)
}
