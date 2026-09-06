package app.pwhs.apexfilemanager.features.explorer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExplorerActivity : BaseActivity() {

    private val viewModel: ExplorerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialPath = intent.getStringExtra(EXTRA_PATH) ?: ""
        if (initialPath.isNotEmpty()) {
            viewModel.onAction(ExplorerUiAction.LoadDirectory(initialPath))
        }

        setBaseContent {
            ApexFileManagerTheme {
                ExplorerScreen(
                    viewModel = viewModel,
                    initialPath = initialPath,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PATH = "extra_path"

        fun createIntent(context: Context, path: String): Intent {
            return Intent(context, ExplorerActivity::class.java).apply {
                putExtra(EXTRA_PATH, path)
            }
        }
    }
}
