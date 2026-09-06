package app.pwhs.apexfilemanager.features.appmanager.apklist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApkListActivity : BaseActivity() {

    private val viewModel: ApkListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            ApexFileManagerTheme {
                ApkListScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ApkListActivity::class.java)
        }
    }
}
