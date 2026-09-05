package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository
import java.io.File

class GetVaultDecryptedFileUseCase(
    private val repository: VaultRepository
) {
    suspend fun getTempFile(vaultItemId: String): Result<File> = repository.getDecryptedTempFile(vaultItemId)
    suspend fun clearTempFiles() = repository.clearDecryptedTempFiles()
}
