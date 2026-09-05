package app.pwhs.apexfilemanager.features.viewer.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImageViewerActivity : BaseActivity() {

    private val viewModel: ImageViewerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra(EXTRA_IMAGE_PATH) ?: ""
        if (savedInstanceState == null && path.isNotEmpty()) {
            viewModel.onAction(ImageViewerUiAction.LoadImage(path))
        }

        setBaseContent {
            ApexFileManagerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"

        fun createIntent(context: Context, path: String): Intent {
            return Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_PATH, path)
            }
        }
    }
}
