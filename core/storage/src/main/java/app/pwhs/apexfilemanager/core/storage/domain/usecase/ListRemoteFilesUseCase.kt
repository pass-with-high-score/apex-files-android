package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.RemoteFileItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository

class ListRemoteFilesUseCase(
    private val repository: NetworkServerRepository
) {
    suspend operator fun invoke(server: NetworkServer, path: String): Result<List<RemoteFileItem>> {
        return repository.listFiles(server, path)
    }
}
