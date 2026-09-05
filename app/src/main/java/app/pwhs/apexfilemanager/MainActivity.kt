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
                    }
                )
            }
        }
    }
}