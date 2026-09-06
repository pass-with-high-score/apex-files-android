package app.pwhs.apexfilemanager.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pwhs.apexfilemanager.R
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume
import app.pwhs.apexfilemanager.features.home.components.MediaCategoriesGrid
import app.pwhs.apexfilemanager.features.home.components.PowerToolsSection
import app.pwhs.apexfilemanager.features.home.components.RecentFilesPreview
import app.pwhs.apexfilemanager.features.home.components.StorageDashboardCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToExplorer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecents: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToCleaner: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToApkList: () -> Unit,
    onNavigateToWifiShare: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenRecentFile: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.RequestPermission -> {
                    context.startActivity(event.intent)
                }
                is HomeUiEvent.NavigateToExplorer -> {
                    onNavigateToExplorer(event.path)
                }
                is HomeUiEvent.NavigateToSearch -> {
                    onNavigateToSearch()
                }
                is HomeUiEvent.NavigateToRecents -> {
                    onNavigateToRecents()
                }
                is HomeUiEvent.NavigateToTrash -> {
                    onNavigateToTrash()
                }
                is HomeUiEvent.NavigateToCleaner -> {
                    onNavigateToCleaner()
                }
                is HomeUiEvent.NavigateToApps -> {
                    onNavigateToApps()
                }
                is HomeUiEvent.NavigateToApkList -> {
                    onNavigateToApkList()
                }
                is HomeUiEvent.NavigateToWifiShare -> {
                    onNavigateToWifiShare()
                }
                is HomeUiEvent.NavigateToNetwork -> {
                    onNavigateToNetwork()
                }
                is HomeUiEvent.NavigateToVault -> {
                    onNavigateToVault()
                }
                is HomeUiEvent.NavigateToSettings -> {
                    onNavigateToSettings()
                }
                is HomeUiEvent.OpenRecentFile -> {
                    onOpenRecentFile(event.item)
                }
                is HomeUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { onAction(HomeUiAction.SearchClick) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.home_search)
                        )
                    }
                    IconButton(onClick = { onAction(HomeUiAction.SettingsClick) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.home_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.volumes.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                !state.hasPermission -> {
                    PermissionRequestBanner(
                        onRequestClick = { onAction(HomeUiAction.RequestPermissionClick) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Storage Volumes Section
                        if (state.volumes.isNotEmpty()) {
                            items(state.volumes, key = { it.id }) { volume ->
                                StorageDashboardCard(
                                    volume = volume,
                                    onClick = { onAction(HomeUiAction.VolumeClick(volume)) },
                                    onCleanClick = { onAction(HomeUiAction.CleanerClick) }
                                )
                            }
                        }

                        // 2. Media Categories Grid
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.home_media_categories),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                MediaCategoriesGrid(
                                    onCategoryClick = { category ->
                                        onAction(HomeUiAction.CategoryClick(category))
                                    }
                                )
                            }
                        }

                        // 3. Recent Files Preview (nếu có)
                        if (state.recentFiles.isNotEmpty()) {
                            item {
                                RecentFilesPreview(
                                    files = state.recentFiles,
                                    onFileClick = { item ->
                                        onAction(HomeUiAction.RecentFileClick(item))
                                    },
                                    onViewAllClick = {
                                        onAction(HomeUiAction.RecentsClick)
                                    }
                                )
                            }
                        }

                        // 4. Power Tools Section
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.home_power_tools),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                PowerToolsSection(
                                    onCleanerClick = { onAction(HomeUiAction.CleanerClick) },
                                    onWifiShareClick = { onAction(HomeUiAction.WifiShareClick) },
                                    onNetworkClick = { onAction(HomeUiAction.NetworkClick) },
                                    onVaultClick = { onAction(HomeUiAction.VaultClick) },
                                    onAppsClick = { onAction(HomeUiAction.AppsClick) }
                                )
                            }
                        }

                        // 5. Privileged Access Section (Root & Shizuku)
                        item {
                            app.pwhs.apexfilemanager.features.home.components.PrivilegedAccessCard(
                                status = state.privilegedStatus,
                                onRequestRootClick = { onAction(HomeUiAction.RequestRootClick) },
                                onRequestShizukuClick = { onAction(HomeUiAction.RequestShizukuClick) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestBanner(
    onRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_grant_permission),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_permission_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestClick) {
                Text(text = stringResource(R.string.home_permission_btn))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    ApexFileManagerTheme {
        HomeContent(
            state = HomeUiState(
                hasPermission = true,
                volumes = listOf(
                    StorageVolume(
                        id = "primary",
                        name = "Bộ nhớ trong",
                        path = "/storage/emulated/0",
                        totalBytes = 128_000_000_000L,
                        freeBytes = 45_000_000_000L,
                        isPrimary = true
                    )
                )
            ),
            onAction = {}
        )
    }
}
