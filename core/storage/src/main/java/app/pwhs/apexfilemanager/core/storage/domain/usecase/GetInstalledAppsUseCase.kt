package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.InstalledApp
import app.pwhs.apexfilemanager.core.storage.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow

class GetInstalledAppsUseCase(
    private val appRepository: AppRepository
) {
    operator fun invoke(includeSystem: Boolean = false): Flow<List<InstalledApp>> {
        return appRepository.getInstalledApps(includeSystem)
    }
}
