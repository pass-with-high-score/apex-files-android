package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository

class UpdateShowFileExtensionsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(show: Boolean) {
        repository.updateShowFileExtensions(show)
    }
}
