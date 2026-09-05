package app.pwhs.apexfilemanager.features.appmanager.list

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pwhs.apexfilemanager.features.appmanager.R
import app.pwhs.apexfilemanager.features.appmanager.list.components.AppItemCard
import java.io.File

@Composable
fun AppManagerScreen(
    viewModel: AppManagerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AppManagerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AppManagerUiEvent.NavigateBack -> {
                    onBackClick()
                }
                is AppManagerUiEvent.LaunchAppIntent -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(event.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Không thể khởi chạy ứng dụng này", Toast.LENGTH_SHORT).show()
                    }
                }
                is AppManagerUiEvent.UninstallAppIntent -> {
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${event.packageName}")
                    }
                    context.startActivity(intent)
                }
                is AppManagerUiEvent.ShareApkFile -> {
                    try {
                        val file = File(event.apkPath)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/vnd.android.package-archive"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, event.appName)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ${event.appName}"))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể chia sẻ tệp APK", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    AppManagerContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerContent(
    state: AppManagerUiState,
    onAction: (AppManagerUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appmanager_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(AppManagerUiAction.LoadApps) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(AppManagerUiAction.SearchQueryChanged(it)) },
                placeholder = { Text(text = stringResource(R.string.appmanager_search_hint)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onAction(AppManagerUiAction.SearchQueryChanged("")) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tabs
            PrimaryTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { onAction(AppManagerUiAction.SelectTab(0)) },
                    text = { Text(text = stringResource(R.string.appmanager_tab_user, state.userApps.size)) }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { onAction(AppManagerUiAction.SelectTab(1)) },
                    text = { Text(text = stringResource(R.string.appmanager_tab_system, state.systemApps.size)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (state.currentDisplayApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.appmanager_empty_apps),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.currentDisplayApps, key = { it.packageName }) { app ->
                            AppItemCard(
                                app = app,
                                isBackingUp = state.backingUpPackage == app.packageName,
                                onBackup = { onAction(AppManagerUiAction.BackupApp(app)) },
                                onShare = { onAction(AppManagerUiAction.ShareApp(app)) },
                                onLaunch = { onAction(AppManagerUiAction.LaunchApp(app)) },
                                onUninstall = { onAction(AppManagerUiAction.UninstallApp(app)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
