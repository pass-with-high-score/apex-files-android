package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository

class GetAppCacheSizeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Long = repository.getCacheSizeBytes()
}
