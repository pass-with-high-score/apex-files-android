package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.AppSettings
import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateShowHiddenFiles(show: Boolean)
    suspend fun updateShowFileExtensions(show: Boolean)
    suspend fun updateDefaultViewMode(viewMode: String)
    suspend fun getCacheSizeBytes(): Long
    suspend fun clearAppCache(): Boolean
}
