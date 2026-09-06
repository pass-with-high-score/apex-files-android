package app.pwhs.apexfilemanager.features.explorer.model

data class PathSegment(
    val name: String,
    val fullPath: String
)

enum class ViewMode {
    LIST,
    GRID
}

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_DESC,
    DATE_ASC,
    SIZE_DESC,
    SIZE_ASC
}

data class ExplorerTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val path: String,
    val title: String,
    val breadcrumbs: List<PathSegment> = emptyList(),
    val files: List<app.pwhs.apexfilemanager.core.storage.domain.model.FileItem> = emptyList(),
    val selectedItems: Set<app.pwhs.apexfilemanager.core.storage.domain.model.FileItem> = emptySet(),
    val isLoading: Boolean = false
)
