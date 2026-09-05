package app.pwhs.apexfilemanager.features.viewer.hex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class HexViewerActivity : BaseActivity() {

    private val viewModel: HexViewerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra(EXTRA_FILE_PATH) ?: ""
        if (savedInstanceState == null && path.isNotEmpty()) {
            viewModel.onAction(HexViewerUiAction.LoadFile(path))
        }

        setBaseContent {
            ApexFileManagerTheme {
                HexViewerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"

        fun createIntent(context: Context, path: String): Intent {
            return Intent(context, HexViewerActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, path)
            }
        }
    }
}
