package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode
import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository

class UpdateThemeModeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        repository.updateThemeMode(themeMode)
    }
}
