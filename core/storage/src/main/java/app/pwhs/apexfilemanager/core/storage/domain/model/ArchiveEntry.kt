package app.pwhs.apexfilemanager.core.storage.domain.model

data class ArchiveEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long,
    val compressedSize: Long,
    val modifiedTimestamp: Long
)
