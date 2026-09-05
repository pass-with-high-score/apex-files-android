package app.pwhs.apexfilemanager.core.storage.domain.repository

import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho việc truy xuất và duyệt tệp tin trong thư mục.
 */
interface FileRepository {
    fun getFilesInDirectory(directoryPath: String, showHidden: Boolean = false): Flow<List<FileItem>>
}
