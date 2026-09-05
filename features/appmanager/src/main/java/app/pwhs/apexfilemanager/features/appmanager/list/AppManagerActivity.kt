package app.pwhs.apexfilemanager.features.appmanager.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppManagerActivity : BaseActivity() {

    private val viewModel: AppManagerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            ApexFileManagerTheme {
                AppManagerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AppManagerActivity::class.java)
        }
    }
}
