package app.pwhs.apexfilemanager.core.storage.domain.model

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val apkPath: String,
    val sizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean,
    val iconBitmapByteArray: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as InstalledApp
        return packageName == other.packageName && versionCode == other.versionCode
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + versionCode.hashCode()
        return result
    }
}
