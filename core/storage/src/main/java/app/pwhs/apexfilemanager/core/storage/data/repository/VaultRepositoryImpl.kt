package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import android.os.Environment
import android.util.Base64
import app.pwhs.apexfilemanager.core.storage.data.vault.CryptoManager
import app.pwhs.apexfilemanager.core.storage.domain.model.VaultItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.crypto.SecretKey

class VaultRepositoryImpl(
    private val context: Context
) : VaultRepository {

    private val vaultDir: File by lazy {
        File(context.filesDir, "vault").apply {
            if (!exists()) mkdirs()
            val noMedia = File(this, ".nomedia")
            if (!noMedia.exists()) noMedia.createNewFile()
        }
    }

    private val configFile: File by lazy { File(vaultDir, "vault_config.json") }
    private val itemsFile: File by lazy { File(vaultDir, "vault_items.json") }
    private val tempDir: File by lazy {
        File(context.cacheDir, "vault_temp").apply {
            if (!exists()) mkdirs()
        }
    }

    private var activeMasterKey: SecretKey? = null

    override suspend fun isVaultSetUp(): Boolean = withContext(Dispatchers.IO) {
        if (!configFile.exists()) return@withContext false
        try {
            val json = JSONObject(configFile.readText())
            json.optBoolean("isPinSet", false)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        if (!configFile.exists()) return@withContext false
        try {
            val json = JSONObject(configFile.readText())
            val saltBase64 = json.getString("salt")
            val encMasterKeyBase64 = json.getString("encryptedMasterKey")

            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            val encMasterKey = Base64.decode(encMasterKeyBase64, Base64.NO_WRAP)

            val pinKey = CryptoManager.deriveKeyFromPin(pin, salt)
            val masterKey = CryptoManager.decryptMasterKey(encMasterKey, pinKey)

            if (masterKey != null) {
                activeMasterKey = masterKey
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setupPin(pin: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val salt = CryptoManager.generateSalt()
            val pinKey = CryptoManager.deriveKeyFromPin(pin, salt)
            val masterKey = CryptoManager.generateMasterKey()
            val encMasterKey = CryptoManager.encryptMasterKey(masterKey, pinKey)

            val json = JSONObject().apply {
                put("isPinSet", true)
                put("isBiometricEnabled", false)
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("encryptedMasterKey", Base64.encodeToString(encMasterKey, Base64.NO_WRAP))
            }
            configFile.writeText(json.toString())
            activeMasterKey = masterKey
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePin(oldPin: String, newPin: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!verifyPin(oldPin)) {
                return@withContext Result.failure(IllegalArgumentException("Old PIN incorrect"))
            }
            val currentMasterKey = activeMasterKey ?: return@withContext Result.failure(IllegalStateException("Master key not unlocked"))

            val newSalt = CryptoManager.generateSalt()
            val newPinKey = CryptoManager.deriveKeyFromPin(newPin, newSalt)
            val encMasterKey = CryptoManager.encryptMasterKey(currentMasterKey, newPinKey)

            val json = if (configFile.exists()) JSONObject(configFile.readText()) else JSONObject()
            json.put("isPinSet", true)
            json.put("salt", Base64.encodeToString(newSalt, Base64.NO_WRAP))
            json.put("encryptedMasterKey", Base64.encodeToString(encMasterKey, Base64.NO_WRAP))

            configFile.writeText(json.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isBiometricEnabled(): Boolean = withContext(Dispatchers.IO) {
        if (!configFile.exists()) return@withContext false
        try {
            val json = JSONObject(configFile.readText())
            json.optBoolean("isBiometricEnabled", false)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!configFile.exists()) return@withContext Result.failure(IllegalStateException("Vault not setup"))
            val json = JSONObject(configFile.readText())
            json.put("isBiometricEnabled", enabled)
            configFile.writeText(json.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaultItems(): List<VaultItem> = withContext(Dispatchers.IO) {
        if (!itemsFile.exists()) return@withContext emptyList()
        try {
            val jsonArray = JSONArray(itemsFile.readText())
            val list = mutableListOf<VaultItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    VaultItem(
                        id = obj.getString("id"),
                        originalName = obj.getString("originalName"),
                        originalPath = obj.getString("originalPath"),
                        encryptedFileName = obj.getString("encryptedFileName"),
                        sizeBytes = obj.getLong("sizeBytes"),
                        mimeType = obj.optString("mimeType", "*/*"),
                        addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.addedTimestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun importFile(sourcePath: String, deleteOriginal: Boolean): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val key = activeMasterKey ?: return@withContext Result.failure(IllegalStateException("Vault locked"))
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return@withContext Result.failure(IllegalArgumentException("File not found"))

            val id = UUID.randomUUID().toString()
            val encFileName = "$id.enc"
            val destEncFile = File(vaultDir, encFileName)

            val success = CryptoManager.encryptFile(sourceFile, destEncFile, key)
            if (!success) {
                return@withContext Result.failure(IllegalStateException("Encryption failed"))
            }

            val item = VaultItem(
                id = id,
                originalName = sourceFile.name,
                originalPath = sourceFile.absolutePath,
                encryptedFileName = encFileName,
                sizeBytes = sourceFile.length(),
                mimeType = getMimeType(sourceFile.name),
                addedTimestamp = System.currentTimeMillis()
            )

            val currentItems = getVaultItems().toMutableList()
            currentItems.add(item)
            saveVaultItems(currentItems)

            if (deleteOriginal) {
                sourceFile.delete()
            }

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportFile(vaultItemId: String, destinationDir: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = activeMasterKey ?: return@withContext Result.failure(IllegalStateException("Vault locked"))
            val items = getVaultItems().toMutableList()
            val item = items.find { it.id == vaultItemId }
                ?: return@withContext Result.failure(IllegalArgumentException("Vault item not found"))

            val encFile = File(vaultDir, item.encryptedFileName)
            if (!encFile.exists()) return@withContext Result.failure(IllegalStateException("Encrypted file missing"))

            val targetDir = if (!destinationDir.isNullOrBlank()) {
                File(destinationDir)
            } else {
                val originalParent = File(item.originalPath).parentFile
                if (originalParent != null && originalParent.exists() && originalParent.canWrite()) {
                    originalParent
                } else {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }
            }

            if (!targetDir.exists()) targetDir.mkdirs()

            var destFile = File(targetDir, item.originalName)
            var count = 1
            val nameWithoutExt = item.originalName.substringBeforeLast(".")
            val ext = item.originalName.substringAfterLast(".", "")
            while (destFile.exists()) {
                val newName = if (ext.isNotEmpty()) "$nameWithoutExt ($count).$ext" else "$nameWithoutExt ($count)"
                destFile = File(targetDir, newName)
                count++
            }

            val success = CryptoManager.decryptFile(encFile, destFile, key)
            if (!success) return@withContext Result.failure(IllegalStateException("Decryption failed"))

            encFile.delete()
            items.removeAll { it.id == vaultItemId }
            saveVaultItems(items)

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVaultItem(vaultItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val items = getVaultItems().toMutableList()
            val item = items.find { it.id == vaultItemId }
                ?: return@withContext Result.failure(IllegalArgumentException("Vault item not found"))

            val encFile = File(vaultDir, item.encryptedFileName)
            if (encFile.exists()) encFile.delete()

            items.removeAll { it.id == vaultItemId }
            saveVaultItems(items)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDecryptedTempFile(vaultItemId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val key = activeMasterKey ?: return@withContext Result.failure(IllegalStateException("Vault locked"))
            val item = getVaultItems().find { it.id == vaultItemId }
                ?: return@withContext Result.failure(IllegalArgumentException("Item not found"))

            val encFile = File(vaultDir, item.encryptedFileName)
            val tempFile = File(tempDir, "${item.id}_${item.originalName}")

            if (!tempFile.exists()) {
                val success = CryptoManager.decryptFile(encFile, tempFile, key)
                if (!success) return@withContext Result.failure(IllegalStateException("Decryption failed"))
            }

            Result.success(tempFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearDecryptedTempFiles() {
        withContext(Dispatchers.IO) {
            try {
                tempDir.listFiles()?.forEach { it.delete() }
            } catch (_: Exception) {}
        }
    }

    private fun saveVaultItems(items: List<VaultItem>) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("originalName", item.originalName)
                put("originalPath", item.originalPath)
                put("encryptedFileName", item.encryptedFileName)
                put("sizeBytes", item.sizeBytes)
                put("mimeType", item.mimeType)
                put("addedTimestamp", item.addedTimestamp)
            }
            jsonArray.put(obj)
        }
        itemsFile.writeText(jsonArray.toString())
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast(".", "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif" -> "image/*"
            "mp4", "mkv", "avi", "mov" -> "video/*"
            "mp3", "wav", "m4a", "flac" -> "audio/*"
            "pdf", "doc", "docx", "txt", "xls", "xlsx" -> "application/pdf"
            else -> "*/*"
        }
    }
}
