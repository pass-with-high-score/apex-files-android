package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import android.content.SharedPreferences
import app.pwhs.apexfilemanager.core.storage.domain.model.AppSettings
import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode
import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getSettings(): Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSettings())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(readSettings())

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.flowOn(Dispatchers.IO)

    private fun readSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val showHidden = prefs.getBoolean(KEY_SHOW_HIDDEN_FILES, false)
        val showExtensions = prefs.getBoolean(KEY_SHOW_EXTENSIONS, true)
        val viewMode = prefs.getString(KEY_DEFAULT_VIEW_MODE, "LIST") ?: "LIST"
        val trashDays = prefs.getInt(KEY_AUTO_CLEAN_TRASH, 30)

        return AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            showHiddenFiles = showHidden,
            showFileExtensions = showExtensions,
            defaultViewMode = viewMode,
            autoCleanTrashDays = trashDays
        )
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
    }

    override suspend fun updateDynamicColor(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    override suspend fun updateShowHiddenFiles(show: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, show).apply()
    }

    override suspend fun updateShowFileExtensions(show: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY_SHOW_EXTENSIONS, show).apply()
    }

    override suspend fun updateDefaultViewMode(viewMode: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_DEFAULT_VIEW_MODE, viewMode).apply()
    }

    override suspend fun getCacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        try {
            var size = calculateDirSize(context.cacheDir)
            context.externalCacheDir?.let {
                size += calculateDirSize(it)
            }
            size
        } catch (_: Exception) {
            0L
        }
    }

    override suspend fun clearAppCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            deleteDirContents(context.cacheDir)
            context.externalCacheDir?.let { deleteDirContents(it) }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
        return size
    }

    private fun deleteDirContents(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteDirContents(file)
            }
            file.delete()
        }
    }

    companion object {
        private const val PREFS_NAME = "apex_file_manager_settings"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_SHOW_HIDDEN_FILES = "pref_show_hidden_files"
        private const val KEY_SHOW_EXTENSIONS = "pref_show_extensions"
        private const val KEY_DEFAULT_VIEW_MODE = "pref_default_view_mode"
        private const val KEY_AUTO_CLEAN_TRASH = "pref_auto_clean_trash_days"
    }
}
