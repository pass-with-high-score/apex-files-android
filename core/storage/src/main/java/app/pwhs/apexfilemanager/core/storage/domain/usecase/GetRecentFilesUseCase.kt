package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase lấy danh sách tệp tin được truy cập/sửa đổi gần đây nhất.
 */
class GetRecentFilesUseCase(
    private val repository: FileRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<FileItem>> {
        return repository.getRecentFiles(limit)
    }
}
