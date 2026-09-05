package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository

class GetVaultItemsUseCase(
    private val repository: VaultRepository
) {
    suspend operator fun invoke(): List<VaultItem> = repository.getVaultItems()
}
