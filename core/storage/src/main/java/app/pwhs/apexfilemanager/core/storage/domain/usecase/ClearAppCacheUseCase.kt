package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository

class ClearAppCacheUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = repository.clearAppCache()
}
