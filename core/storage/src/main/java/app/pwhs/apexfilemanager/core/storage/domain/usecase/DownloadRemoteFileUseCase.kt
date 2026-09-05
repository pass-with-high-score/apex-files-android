package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository
import java.io.File

class DownloadRemoteFileUseCase(
    private val repository: NetworkServerRepository
) {
    suspend operator fun invoke(server: NetworkServer, remotePath: String, destFile: File): Result<Unit> {
        return repository.downloadFile(server, remotePath, destFile)
    }
}
