package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.JunkFile
import app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository
import kotlinx.coroutines.flow.Flow

class GetJunkFilesUseCase(
    private val cleanerRepository: CleanerRepository
) {
    operator fun invoke(): Flow<List<JunkFile>> {
        return cleanerRepository.getJunkFiles()
    }
}
