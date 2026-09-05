package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase lấy danh sách tệp tin và thư mục con trong một thư mục chỉ định.
 */
class GetDirectoryContentsUseCase(
    private val repository: FileRepository
) {
    operator fun invoke(directoryPath: String, showHidden: Boolean = false): Flow<List<FileItem>> {
        return repository.getFilesInDirectory(directoryPath, showHidden)
    }
}
