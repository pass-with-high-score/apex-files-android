package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository

class DeleteNetworkServerUseCase(
    private val repository: NetworkServerRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteServer(id)
}
