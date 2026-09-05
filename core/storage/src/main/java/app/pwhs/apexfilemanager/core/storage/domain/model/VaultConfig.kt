package app.pwhs.apexfilemanager.core.storage.domain.model

data class VaultConfig(
    val isPinSet: Boolean,
    val isBiometricEnabled: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = ""
)
