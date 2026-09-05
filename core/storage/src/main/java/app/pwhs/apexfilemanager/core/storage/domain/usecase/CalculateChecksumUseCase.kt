package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ChecksumResult(
    val md5: String,
    val sha1: String,
    val sha256: String
)

class CalculateChecksumUseCase {
    suspend operator fun invoke(filePath: String): Result<ChecksumResult> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(filePath)
            if (!file.exists() || file.isDirectory) {
                throw IllegalArgumentException("Tệp tin không tồn tại hoặc là thư mục")
            }
            ChecksumResult(
                md5 = HashUtil.calculateHash(file, "MD5"),
                sha1 = HashUtil.calculateHash(file, "SHA-1"),
                sha256 = HashUtil.calculateHash(file, "SHA-256")
            )
        }
    }
}
