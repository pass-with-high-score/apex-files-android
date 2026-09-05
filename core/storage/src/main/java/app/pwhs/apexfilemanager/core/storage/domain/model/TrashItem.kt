package app.pwhs.apexfilemanager.core.storage.domain.model

data class TrashItem(
    val id: String,
    val name: String,
    val originalPath: String,
    val trashPath: String,
    val sizeBytes: Long,
    val deletedTimestamp: Long,
    val isDirectory: Boolean
)
