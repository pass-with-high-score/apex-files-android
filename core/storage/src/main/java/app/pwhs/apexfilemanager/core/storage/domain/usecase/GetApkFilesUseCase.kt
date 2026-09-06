package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase lấy danh sách các tệp cài đặt APK có trên thiết bị.
 * Pure Kotlin, tuân thủ Clean Architecture.
 */
class GetApkFilesUseCase(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<FileItem>> {
        return repository.getApkFiles()
    }
}
