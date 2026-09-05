package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository
import kotlinx.coroutines.flow.Flow

class GetNetworkServersUseCase(
    private val repository: NetworkServerRepository
) {
    operator fun invoke(): Flow<List<NetworkServer>> = repository.getServers()
}
