package app.pwhs.apexfilemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.features.explorer.ExplorerActivity
import app.pwhs.apexfilemanager.features.home.HomeScreen
import app.pwhs.apexfilemanager.features.home.HomeViewModel
import app.pwhs.apexfilemanager.features.search.SearchActivity
import app.pwhs.apexfilemanager.features.settings.SettingsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent {
            ApexFileManagerTheme {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToExplorer = { path ->
                        startActivity(ExplorerActivity.createIntent(this, path))
                    },
                    onNavigateToSearch = {
                        startActivity(SearchActivity.createIntent(this))
                    },
                    onNavigateToRecents = {
                        startActivity(app.pwhs.apexfilemanager.features.recents.RecentsActivity.createIntent(this))
                    },
                    onNavigateToTrash = {
                        startActivity(app.pwhs.apexfilemanager.features.cleaner.trash.TrashActivity.createIntent(this))
                    },
                    onNavigateToCleaner = {
                        startActivity(app.pwhs.apexfilemanager.features.cleaner.analysis.CleanerActivity.createIntent(this))
                    },
                    onNavigateToApps = {
                        startActivity(app.pwhs.apexfilemanager.features.appmanager.list.AppManagerActivity.createIntent(this))
                    },
                    onNavigateToApkList = {
                        startActivity(app.pwhs.apexfilemanager.features.appmanager.apklist.ApkListActivity.createIntent(this))
                    },
                    onNavigateToWifiShare = {
                        startActivity(app.pwhs.apexfilemanager.features.wifishare.WifiShareActivity.createIntent(this))
                    },
                    onNavigateToNetwork = {
                        startActivity(app.pwhs.apexfilemanager.features.network.list.NetworkServerActivity.createIntent(this))
                    },
                    onNavigateToVault = {
                        startActivity(Intent(this, app.pwhs.apexfilemanager.features.vault.auth.VaultAuthActivity::class.java))
                    },
                    onNavigateToSettings = {
                        startActivity(SettingsActivity.createIntent(this))
                    },
                    onOpenRecentFile = { file ->
                        handleOpenFile(file)
                    }
                )
            }
        }
    }

    private fun handleOpenFile(item: app.pwhs.apexfilemanager.core.storage.domain.model.FileItem) {
        val ext = item.name.substringAfterLast('.', "").lowercase()
        try {
            when {
                ext == "apk" -> {
                    val clazz = Class.forName("app.pwhs.apexfilemanager.features.appmanager.detail.ApkDetailActivity")
                    val intent = Intent(this, clazz).apply {
                        putExtra("extra_apk_path", item.path)
                    }
                    startActivity(intent)
                }
                ext in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "jar") -> {
                    val clazz = Class.forName("app.pwhs.apexfilemanager.features.archive.ArchiveActivity")
                    val intent = Intent(this, clazz).apply {
                        putExtra("extra_archive_path", item.path)
                    }
                    startActivity(intent)
                }
                item.mimeType.startsWith("image/") || ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp") -> {
                    val clazz = Class.forName("app.pwhs.apexfilemanager.features.viewer.image.ImageViewerActivity")
                    val intent = Intent(this, clazz).apply {
                        putExtra("extra_image_path", item.path)
                    }
                    startActivity(intent)
                }
                item.mimeType.startsWith("text/") || ext in setOf(
                    "txt", "json", "xml", "html", "htm", "css", "js", "ts", "kt", "java",
                    "py", "c", "cpp", "h", "md", "log", "properties", "gradle", "sh", "yml",
                    "yaml", "ini", "conf", "env", "sql", "csv"
                ) -> {
                    val clazz = Class.forName("app.pwhs.apexfilemanager.features.viewer.text.TextEditorActivity")
                    val intent = Intent(this, clazz).apply {
                        putExtra("extra_file_path", item.path)
                    }
                    startActivity(intent)
                }
                else -> {
                    // Navigate to folder containing file in Explorer
                    val parentPath = item.path.substringBeforeLast('/')
                    startActivity(ExplorerActivity.createIntent(this, parentPath))
                }
            }
        } catch (_: Exception) {
            val parentPath = item.path.substringBeforeLast('/')
            startActivity(ExplorerActivity.createIntent(this, parentPath))
        }
    }
}