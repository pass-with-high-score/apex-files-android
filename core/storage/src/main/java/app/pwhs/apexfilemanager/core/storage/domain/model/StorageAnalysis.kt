package app.pwhs.apexfilemanager.core.storage.domain.model

data class StorageCategory(
    val name: String,
    val sizeBytes: Long,
    val count: Int,
    val percentage: Float
)

data class StorageAnalysis(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val imagesBytes: Long = 0L,
    val videosBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val documentsBytes: Long = 0L,
    val archivesBytes: Long = 0L,
    val apksBytes: Long = 0L,
    val otherBytes: Long = 0L
)

data class JunkFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val reason: String
)
