package app.pwhs.apexfilemanager.core.storage.domain.model

enum class ServerProtocol(val defaultPort: Int, val displayName: String) {
    SMB(445, "SMB / Windows Share"),
    FTP(21, "FTP"),
    FTPS(21, "FTPS (Explicit SSL)"),
    SFTP(22, "SFTP (SSH)")
}

data class NetworkServer(
    val id: String,
    val name: String,
    val protocol: ServerProtocol,
    val host: String,
    val port: Int = protocol.defaultPort,
    val username: String = "",
    val password: String = "",
    val isAnonymous: Boolean = false,
    val shareOrPath: String = ""
)
