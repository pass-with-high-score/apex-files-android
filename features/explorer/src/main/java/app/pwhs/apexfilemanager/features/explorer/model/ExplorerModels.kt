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
