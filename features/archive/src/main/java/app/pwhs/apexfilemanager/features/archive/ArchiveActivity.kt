package app.pwhs.apexfilemanager.features.archive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveActivity : BaseActivity() {

    private val viewModel: ArchiveViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val archivePath = intent.getStringExtra(EXTRA_ARCHIVE_PATH) ?: ""
        if (savedInstanceState == null && archivePath.isNotEmpty()) {
            viewModel.onAction(ArchiveUiAction.LoadArchive(archivePath))
        }

        setBaseContent {
            ApexFileManagerTheme {
                ArchiveScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ARCHIVE_PATH = "extra_archive_path"

        fun createIntent(context: Context, archivePath: String): Intent {
            return Intent(context, ArchiveActivity::class.java).apply {
                putExtra(EXTRA_ARCHIVE_PATH, archivePath)
            }
        }
    }
}
