package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.AppRepository

class BackupAppUseCase(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): Result<String> {
        return appRepository.backupApp(packageName)
    }
}
