package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository

class SaveNetworkServerUseCase(
    private val repository: NetworkServerRepository
) {
    suspend operator fun invoke(server: NetworkServer) = repository.saveServer(server)
}
