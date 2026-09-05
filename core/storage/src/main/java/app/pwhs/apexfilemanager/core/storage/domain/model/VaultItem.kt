package app.pwhs.apexfilemanager.core.storage.domain.model

data class VaultItem(
    val id: String,
    val originalName: String,
    val originalPath: String,
    val encryptedFileName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)
