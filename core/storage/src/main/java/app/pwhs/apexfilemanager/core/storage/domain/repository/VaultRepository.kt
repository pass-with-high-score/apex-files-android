package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import java.io.File

interface VaultRepository {
    suspend fun isVaultSetUp(): Boolean
    suspend fun verifyPin(pin: String): Boolean
    suspend fun setupPin(pin: String): Result<Unit>
    suspend fun changePin(oldPin: String, newPin: String): Result<Unit>
    suspend fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>
    suspend fun getVaultItems(): List<VaultItem>
    suspend fun importFile(sourcePath: String, deleteOriginal: Boolean = true): Result<VaultItem>
    suspend fun exportFile(vaultItemId: String, destinationDir: String? = null): Result<String>
    suspend fun deleteVaultItem(vaultItemId: String): Result<Unit>
    suspend fun getDecryptedTempFile(vaultItemId: String): Result<File>
    suspend fun clearDecryptedTempFiles()
}
