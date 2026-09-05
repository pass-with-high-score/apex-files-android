package app.pwhs.apexfilemanager.features.viewer.text

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class TextEditorActivity : BaseActivity() {

    private val viewModel: TextEditorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra(EXTRA_FILE_PATH) ?: ""
        if (savedInstanceState == null && path.isNotEmpty()) {
            viewModel.onAction(TextEditorUiAction.LoadFile(path))
        }

        setBaseContent {
            ApexFileManagerTheme {
                TextEditorScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"

        fun createIntent(context: Context, path: String): Intent {
            return Intent(context, TextEditorActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, path)
            }
        }
    }
}
