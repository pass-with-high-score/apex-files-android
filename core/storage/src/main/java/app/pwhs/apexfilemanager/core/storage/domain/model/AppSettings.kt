package app.pwhs.apexfilemanager.core.storage.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val showHiddenFiles: Boolean = false,
    val showFileExtensions: Boolean = true,
    val defaultViewMode: String = "LIST",
    val autoCleanTrashDays: Int = 30
)
