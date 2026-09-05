package app.pwhs.apexfilemanager.features.appmanager.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApkDetailActivity : BaseActivity() {

    private val viewModel: ApkDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra(EXTRA_APK_PATH) ?: ""
        if (savedInstanceState == null && path.isNotEmpty()) {
            viewModel.onAction(ApkDetailUiAction.LoadApk(path))
        }

        setBaseContent {
            ApexFileManagerTheme {
                ApkDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onOpenArchive = { archivePath ->
                        try {
                            val clazz = Class.forName("app.pwhs.apexfilemanager.features.archive.ArchiveActivity")
                            val intent = Intent(this, clazz).apply {
                                putExtra("extra_archive_path", archivePath)
                            }
                            startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(this, "Không thể mở tệp nén", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_APK_PATH = "extra_apk_path"

        fun createIntent(context: Context, path: String): Intent {
            return Intent(context, ApkDetailActivity::class.java).apply {
                putExtra(EXTRA_APK_PATH, path)
            }
        }
    }
}
