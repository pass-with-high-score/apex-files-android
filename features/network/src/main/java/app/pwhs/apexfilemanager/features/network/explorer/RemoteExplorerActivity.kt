package app.pwhs.apexfilemanager.features.network.explorer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoteExplorerActivity : BaseActivity() {

    private val viewModel: RemoteExplorerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serverId = intent.getStringExtra(EXTRA_SERVER_ID) ?: ""
        if (savedInstanceState == null && serverId.isNotEmpty()) {
            viewModel.onAction(RemoteExplorerUiAction.Init(serverId))
        }

        setBaseContent {
            ApexFileManagerTheme {
                RemoteExplorerScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_SERVER_ID = "extra_server_id"

        fun createIntent(context: Context, serverId: String): Intent {
            return Intent(context, RemoteExplorerActivity::class.java).apply {
                putExtra(EXTRA_SERVER_ID, serverId)
            }
        }
    }
}
