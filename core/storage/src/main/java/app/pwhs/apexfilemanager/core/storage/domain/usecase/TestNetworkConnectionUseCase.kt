package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository

class TestNetworkConnectionUseCase(
    private val repository: NetworkServerRepository
) {
    suspend operator fun invoke(server: NetworkServer): Result<Unit> = repository.testConnection(server)
}
