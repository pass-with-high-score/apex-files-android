package app.pwhs.apexfilemanager.features.recents

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentsActivity : BaseActivity() {

    private val viewModel: RecentsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            ApexFileManagerTheme {
                RecentsScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, RecentsActivity::class.java)
        }
    }
}
