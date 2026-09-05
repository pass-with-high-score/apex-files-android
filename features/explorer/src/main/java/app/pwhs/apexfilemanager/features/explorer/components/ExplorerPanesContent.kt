package app.pwhs.apexfilemanager.features.explorer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.features.explorer.ActivePane
import app.pwhs.apexfilemanager.features.explorer.ExplorerUiAction
import app.pwhs.apexfilemanager.features.explorer.ExplorerUiState

@Composable
fun ExplorerPanesContent(
    state: ExplorerUiState,
    onAction: (ExplorerUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (!state.isDualPaneMode) {
            // Single Pane Mode
            Column(modifier = Modifier.fillMaxSize()) {
                BreadcrumbBar(
                    breadcrumbs = state.breadcrumbs,
                    onSegmentClick = { onAction(ExplorerUiAction.BreadcrumbClick(it)) }
                )
                PaneFileList(
                    files = state.files,
                    viewMode = state.viewMode,
                    selectedItems = state.selectedItems,
                    isSelectionMode = state.isSelectionMode,
                    onFileClick = { onAction(ExplorerUiAction.FileClick(it)) },
                    onFileLongClick = { onAction(ExplorerUiAction.ToggleSelect(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Dual Pane Mode (Side by Side)
            Row(modifier = Modifier.fillMaxSize()) {
                // Pane 1 (Primary)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onAction(ExplorerUiAction.SwitchActivePane(ActivePane.PRIMARY)) }
                        .then(
                            if (state.activePane == ActivePane.PRIMARY)
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                ) {
                    BreadcrumbBar(
                        breadcrumbs = state.breadcrumbs,
                        onSegmentClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.PRIMARY))
                            onAction(ExplorerUiAction.BreadcrumbClick(it))
                        }
                    )
                    PaneFileList(
                        files = state.files,
                        viewMode = state.viewMode,
                        selectedItems = state.selectedItems,
                        isSelectionMode = state.isSelectionMode && state.activePane == ActivePane.PRIMARY,
                        onFileClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.PRIMARY))
                            onAction(ExplorerUiAction.FileClick(it))
                        },
                        onFileLongClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.PRIMARY))
                            onAction(ExplorerUiAction.ToggleSelect(it))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Pane 2 (Secondary)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onAction(ExplorerUiAction.SwitchActivePane(ActivePane.SECONDARY)) }
                        .then(
                            if (state.activePane == ActivePane.SECONDARY)
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                ) {
                    BreadcrumbBar(
                        breadcrumbs = state.secondaryBreadcrumbs,
                        onSegmentClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.SECONDARY))
                            onAction(ExplorerUiAction.BreadcrumbClick(it))
                        }
                    )
                    PaneFileList(
                        files = state.secondaryFiles,
                        viewMode = state.viewMode,
                        selectedItems = state.secondarySelectedItems,
                        isSelectionMode = state.isSelectionMode && state.activePane == ActivePane.SECONDARY,
                        onFileClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.SECONDARY))
                            onAction(ExplorerUiAction.FileClick(it))
                        },
                        onFileLongClick = {
                            onAction(ExplorerUiAction.SwitchActivePane(ActivePane.SECONDARY))
                            onAction(ExplorerUiAction.ToggleSelect(it))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (state.isLoading || state.secondaryIsLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
