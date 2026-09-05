package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.SearchCategory
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class SearchFilesUseCase(
    private val fileRepository: FileRepository
) {
    operator fun invoke(
        rootPath: String,
        query: String,
        category: SearchCategory = SearchCategory.ALL,
        showHidden: Boolean = false
    ): Flow<List<FileItem>> {
        return fileRepository.searchFiles(rootPath, query, category, showHidden)
    }
}
