package app.pwhs.apexfilemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.features.explorer.ExplorerActivity
import app.pwhs.apexfilemanager.features.home.HomeScreen
import app.pwhs.apexfilemanager.features.home.HomeViewModel
import app.pwhs.apexfilemanager.features.search.SearchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent {
            ApexFileManagerTheme {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToExplorer = { path ->
                        startActivity(ExplorerActivity.createIntent(this, path))
                    },
                    onNavigateToSearch = {
                        startActivity(SearchActivity.createIntent(this))
                    },
                    onNavigateToRecents = {
                        startActivity(app.pwhs.apexfilemanager.features.recents.RecentsActivity.createIntent(this))
                    },
                    onNavigateToTrash = {
                        startActivity(app.pwhs.apexfilemanager.features.cleaner.trash.TrashActivity.createIntent(this))
                    },
                    onNavigateToCleaner = {
                        startActivity(app.pwhs.apexfilemanager.features.cleaner.analysis.CleanerActivity.createIntent(this))
                    },
                    onNavigateToApps = {
                        startActivity(app.pwhs.apexfilemanager.features.appmanager.list.AppManagerActivity.createIntent(this))
                    },
                    onNavigateToWifiShare = {
                        startActivity(app.pwhs.apexfilemanager.features.wifishare.WifiShareActivity.createIntent(this))
                    },
                    onNavigateToNetwork = {
                        startActivity(app.pwhs.apexfilemanager.features.network.list.NetworkServerActivity.createIntent(this))
                    },
                    onNavigateToVault = {
                        startActivity(Intent(this, app.pwhs.apexfilemanager.features.vault.auth.VaultAuthActivity::class.java))
                    }
                )
            }
        }
    }
}