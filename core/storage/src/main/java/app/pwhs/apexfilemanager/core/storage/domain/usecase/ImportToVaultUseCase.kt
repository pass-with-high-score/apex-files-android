package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class ImportToVaultUseCase(
    private val repository: VaultRepository
) {
    suspend operator fun invoke(sourcePath: String, deleteOriginal: Boolean = true): Result<VaultItem> =
        repository.importFile(sourcePath, deleteOriginal)
}
