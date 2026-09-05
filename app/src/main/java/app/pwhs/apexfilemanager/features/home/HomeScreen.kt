package app.pwhs.apexfilemanager.features.home

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import app.pwhs.apexfilemanager.features.home.components.QuickAccessCard
import app.pwhs.apexfilemanager.features.home.components.StorageVolumeCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import app.pwhs.apexfilemanager.core.storage.domain.model.StorageVolume

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToExplorer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecents: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToCleaner: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToWifiShare: () -> Unit,
    onNavigateToNetwork: () -> Unit,
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
                is HomeUiEvent.NavigateToWifiShare -> {
                    onNavigateToWifiShare()
                }
                is HomeUiEvent.NavigateToNetwork -> {
                    onNavigateToNetwork()
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
                state.isLoading -> {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.home_storage_overview),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (state.volumes.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.home_empty_storage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(state.volumes, key = { it.id }) { volume ->
                                StorageVolumeCard(
                                    volume = volume,
                                    onClick = { onAction(HomeUiAction.VolumeClick(volume)) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.home_quick_access),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.AccessTime,
                                title = stringResource(R.string.home_recents_title),
                                description = stringResource(R.string.home_recents_desc),
                                onClick = { onAction(HomeUiAction.RecentsClick) }
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.CleaningServices,
                                title = stringResource(R.string.home_cleaner_title),
                                description = stringResource(R.string.home_cleaner_desc),
                                onClick = { onAction(HomeUiAction.CleanerClick) }
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.DeleteSweep,
                                title = stringResource(R.string.home_trash_title),
                                description = stringResource(R.string.home_trash_desc),
                                onClick = { onAction(HomeUiAction.TrashClick) }
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.Android,
                                title = stringResource(R.string.home_apps_title),
                                description = stringResource(R.string.home_apps_desc),
                                onClick = { onAction(HomeUiAction.AppsClick) }
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.Wifi,
                                title = stringResource(R.string.home_wifishare_title),
                                description = stringResource(R.string.home_wifishare_desc),
                                onClick = { onAction(HomeUiAction.WifiShareClick) }
                            )
                        }

                        item {
                            QuickAccessCard(
                                icon = Icons.Default.Dns,
                                title = stringResource(R.string.home_network_title),
                                description = stringResource(R.string.home_network_desc),
                                onClick = { onAction(HomeUiAction.NetworkClick) }
                            )
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
