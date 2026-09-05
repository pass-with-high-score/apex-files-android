package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.ApkInfo
import app.pwhs.apexfilemanager.core.storage.domain.repository.AppRepository

class GetApkInfoUseCase(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(path: String): Result<ApkInfo> {
        return appRepository.getApkInfo(path)
    }
}
