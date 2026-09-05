package app.pwhs.apexfilemanager.core.storage.domain.model

data class ApkInfo(
    val filePath: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val sizeBytes: Long,
    val iconBitmapByteArray: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApkInfo
        return filePath == other.filePath && packageName == other.packageName && versionCode == other.versionCode
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + versionCode.hashCode()
        return result
    }
}
