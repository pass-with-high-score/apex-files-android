package app.pwhs.apexfilemanager.features.explorer

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pwhs.apexfilemanager.core.designsystem.theme.ApexFileManagerTheme
import app.pwhs.apexfilemanager.core.storage.domain.model.FileItem
import app.pwhs.apexfilemanager.features.explorer.components.BreadcrumbBar
import app.pwhs.apexfilemanager.features.explorer.components.FileListItem
import app.pwhs.apexfilemanager.features.explorer.model.SortOption

@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExplorerUiEvent.OpenFileExternal -> {
                    Toast.makeText(context, event.path, Toast.LENGTH_SHORT).show()
                }
                is ExplorerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ExplorerContent(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerContent(
    state: ExplorerUiState,
    onAction: (ExplorerUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.explorer_title),
                        fontWeight = FontWeight.Bold
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
                    IconButton(onClick = { onAction(ExplorerUiAction.ToggleHiddenFiles) }) {
                        Icon(
                            imageVector = if (state.showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.explorer_hidden_files)
                        )
                    }

                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.explorer_sort)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_name_asc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.NAME_ASC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_date_desc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.DATE_DESC))
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.explorer_sort_size_desc)) },
                            onClick = {
                                onAction(ExplorerUiAction.ChangeSort(SortOption.SIZE_DESC))
                                showSortMenu = false
                            }
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
            if (state.breadcrumbs.isNotEmpty()) {
                BreadcrumbBar(
                    breadcrumbs = state.breadcrumbs,
                    onSegmentClick = { path -> onAction(ExplorerUiAction.BreadcrumbClick(path)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.files.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.explorer_empty_folder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.files, key = { it.id }) { item ->
                                FileListItem(
                                    item = item,
                                    onClick = { onAction(ExplorerUiAction.FileClick(item)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExplorerContentPreview() {
    ApexFileManagerTheme {
        ExplorerContent(
            state = ExplorerUiState(
                currentPath = "/storage/emulated/0/Download",
                files = listOf(
                    FileItem(
                        id = "1",
                        name = "Documents",
                        path = "/storage/emulated/0/Download/Documents",
                        isDirectory = true
                    ),
                    FileItem(
                        id = "2",
                        name = "sample_report.pdf",
                        path = "/storage/emulated/0/Download/sample_report.pdf",
                        sizeBytes = 2_450_000L,
                        isDirectory = false
                    )
                )
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}
