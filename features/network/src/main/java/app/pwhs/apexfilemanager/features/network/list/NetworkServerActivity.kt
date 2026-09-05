package app.pwhs.apexfilemanager.features.network.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.features.network.explorer.RemoteExplorerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class NetworkServerActivity : BaseActivity() {

    private val viewModel: NetworkServerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setBaseContent {
            ApexFileManagerTheme {
                NetworkServerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onServerClick = { serverId ->
                        startActivity(RemoteExplorerActivity.createIntent(this, serverId))
                    }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NetworkServerActivity::class.java)
        }
    }
}
