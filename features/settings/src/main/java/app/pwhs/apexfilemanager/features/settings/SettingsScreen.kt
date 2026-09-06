package app.pwhs.apexfilemanager.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode
import app.pwhs.apexfilemanager.features.settings.components.SettingGroupTitle
import app.pwhs.apexfilemanager.features.settings.components.SettingItemRow
import app.pwhs.apexfilemanager.features.settings.components.SettingSwitchRow
import app.pwhs.apexfilemanager.features.settings.components.ThemeSelectionDialog

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.NavigateBack -> onBackClick()
                is SettingsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsUiEvent.OpenSystemPermissionSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    SettingsContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )

    if (state.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = state.settings.themeMode,
            onSelectTheme = { viewModel.onAction(SettingsUiAction.SelectTheme(it)) },
            onDismiss = { viewModel.onAction(SettingsUiAction.ToggleThemeDialog(false)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onAction: (SettingsUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(SettingsUiAction.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_title)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Giao diện & Hiển thị
            item {
                SettingGroupTitle(title = stringResource(R.string.settings_section_display))
            }
            item {
                val themeLabel = when (state.settings.themeMode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                }
                SettingItemRow(
                    title = stringResource(R.string.settings_theme_title),
                    description = themeLabel,
                    icon = Icons.Default.DarkMode,
                    onClick = { onAction(SettingsUiAction.ToggleThemeDialog(true)) }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.settings_dynamic_color_title),
                        description = stringResource(R.string.settings_dynamic_color_desc),
                        icon = Icons.Default.ColorLens,
                        checked = state.settings.dynamicColor,
                        onCheckedChange = { onAction(SettingsUiAction.ToggleDynamicColor(it)) }
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // 2. Quản lý tệp tin
            item {
                SettingGroupTitle(title = stringResource(R.string.settings_section_files))
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.settings_show_hidden_title),
                    description = stringResource(R.string.settings_show_hidden_desc),
                    icon = Icons.Default.Visibility,
                    checked = state.settings.showHiddenFiles,
                    onCheckedChange = { onAction(SettingsUiAction.ToggleShowHiddenFiles(it)) }
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.settings_show_extensions_title),
                    description = stringResource(R.string.settings_show_extensions_desc),
                    icon = Icons.Default.Extension,
                    checked = state.settings.showFileExtensions,
                    onCheckedChange = { onAction(SettingsUiAction.ToggleShowFileExtensions(it)) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // 3. Đặc quyền & Quyền hạn
            item {
                SettingGroupTitle(title = stringResource(R.string.settings_section_system))
            }
            item {
                val rootStatus = if (state.privilegedStatus.isRootGranted) {
                    "✓ Đã cấp quyền"
                } else if (state.privilegedStatus.isRootAvailable) {
                    "Khả dụng (Chưa cấp quyền)"
                } else {
                    "Không khả dụng"
                }
                SettingItemRow(
                    title = stringResource(R.string.settings_root_title),
                    description = "$rootStatus - ${stringResource(R.string.settings_root_desc)}",
                    icon = Icons.Default.AdminPanelSettings
                )
            }
            item {
                val shizukuStatus = if (state.privilegedStatus.isShizukuGranted) {
                    "✓ Đang hoạt động"
                } else if (state.privilegedStatus.isShizukuAvailable) {
                    "Khả dụng (Chưa cấp quyền)"
                } else {
                    "Chưa kết nối"
                }
                SettingItemRow(
                    title = stringResource(R.string.settings_shizuku_title),
                    description = "$shizukuStatus - ${stringResource(R.string.settings_shizuku_desc)}",
                    icon = Icons.Default.Terminal
                )
            }
            item {
                SettingItemRow(
                    title = stringResource(R.string.settings_permissions_title),
                    description = stringResource(R.string.settings_permissions_desc),
                    icon = Icons.Default.Security,
                    onClick = { onAction(SettingsUiAction.ManagePermissions) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // 4. Bộ nhớ & Dữ liệu tạm
            item {
                SettingGroupTitle(title = stringResource(R.string.settings_section_storage))
            }
            item {
                val cacheFormatted = Formatter.formatFileSize(context, state.cacheSizeBytes)
                SettingItemRow(
                    title = stringResource(R.string.settings_clear_cache_title),
                    description = stringResource(R.string.settings_cache_size_format, cacheFormatted),
                    icon = Icons.Default.CleaningServices,
                    onClick = { onAction(SettingsUiAction.ClearCache) },
                    trailingContent = {
                        if (state.isClearingCache) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // 5. Thông tin & Hỗ trợ
            item {
                SettingGroupTitle(title = stringResource(R.string.settings_section_about))
            }
            item {
                SettingItemRow(
                    title = stringResource(R.string.settings_version_title),
                    description = stringResource(R.string.settings_version_desc),
                    icon = Icons.Default.Info
                )
            }
            item {
                SettingItemRow(
                    title = stringResource(R.string.settings_github_title),
                    description = stringResource(R.string.settings_github_desc),
                    icon = Icons.Default.Folder
                )
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
