package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository

class UpdateShowHiddenFilesUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(show: Boolean) {
        repository.updateShowHiddenFiles(show)
    }
}
