package app.pwhs.apexfilemanager.core.storage.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import app.pwhs.apexfilemanager.core.storage.domain.model.ApkInfo
import app.pwhs.apexfilemanager.core.storage.domain.model.InstalledApp
import app.pwhs.apexfilemanager.core.storage.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class AppRepositoryImpl(
    private val context: Context
) : AppRepository {

    private val packageManager: PackageManager
        get() = context.packageManager

    override suspend fun getApkInfo(path: String): Result<ApkInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(path)
            if (!file.exists()) throw IllegalArgumentException("Tệp APK không tồn tại: $path")

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_META_DATA
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES or PackageManager.GET_META_DATA
            }

            val pkgInfo = packageManager.getPackageArchiveInfo(path, flags)
                ?: throw IllegalStateException("Không thể phân tích gói APK")

            val appInfo = pkgInfo.applicationInfo
                ?: throw IllegalStateException("Không thể lấy thông tin ứng dụng từ gói APK")
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path

            val appName = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                file.nameWithoutExtension
            }

            val iconBytes = try {
                val drawable = packageManager.getApplicationIcon(appInfo)
                drawableToByteArray(drawable)
            } catch (_: Exception) {
                null
            }

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }

            val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appInfo.minSdkVersion
            } else {
                24
            }

            ApkInfo(
                filePath = path,
                appName = appName,
                packageName = pkgInfo.packageName,
                versionName = pkgInfo.versionName ?: "1.0",
                versionCode = versionCode,
                minSdk = minSdk,
                targetSdk = appInfo.targetSdkVersion,
                sizeBytes = file.length(),
                iconBitmapByteArray = iconBytes
            )
        }
    }

    override fun getInstalledApps(includeSystem: Boolean): Flow<List<InstalledApp>> = flow {
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
            } else {
                packageManager.getInstalledPackages(0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val list = mutableListOf<InstalledApp>()
        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!includeSystem && isSystem) continue

            val appName = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.packageName
            }

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkg.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkg.versionCode.toLong()
            }

            val apkFile = File(appInfo.sourceDir)
            val size = if (apkFile.exists()) apkFile.length() else 0L

            val iconBytes = try {
                val drawable = packageManager.getApplicationIcon(appInfo)
                drawableToByteArray(drawable)
            } catch (_: Exception) {
                null
            }

            list.add(
                InstalledApp(
                    appName = appName,
                    packageName = pkg.packageName,
                    versionName = pkg.versionName ?: "1.0",
                    versionCode = versionCode,
                    apkPath = appInfo.sourceDir,
                    sizeBytes = size,
                    firstInstallTime = pkg.firstInstallTime,
                    lastUpdateTime = pkg.lastUpdateTime,
                    isSystemApp = isSystem,
                    iconBitmapByteArray = iconBytes
                )
            )
        }
        emit(list.sortedBy { it.appName.lowercase() })
    }.flowOn(Dispatchers.IO)

    override suspend fun backupApp(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val sourceApk = File(appInfo.sourceDir)
            if (!sourceApk.exists()) throw IllegalStateException("Không tìm thấy tệp APK gốc")

            val appName = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                packageName
            }

            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = pkgInfo.versionName ?: "1.0"

            val backupDir = File(Environment.getExternalStorageDirectory(), "ApexFileManager/Backup")
            if (!backupDir.exists()) backupDir.mkdirs()

            val cleanName = appName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val targetApk = File(backupDir, "${cleanName}_v${versionName}.apk")

            sourceApk.copyTo(targetApk, overwrite = true)
            targetApk.absolutePath
        }
    }

    private fun drawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
