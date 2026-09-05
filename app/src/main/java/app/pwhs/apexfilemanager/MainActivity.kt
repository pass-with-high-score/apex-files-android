package app.pwhs.apexfilemanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.features.home.HomeScreen
import app.pwhs.apexfilemanager.features.home.HomeViewModel
import app.pwhs.apexfilemanager.features.home.HomeViewModelFactory

class MainActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent {
            ApexFileManagerTheme {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToExplorer = { path ->
                        // Sẽ khởi chạy ExplorerActivity qua Intent ở Phase tiếp theo
                        Toast.makeText(this, path, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}