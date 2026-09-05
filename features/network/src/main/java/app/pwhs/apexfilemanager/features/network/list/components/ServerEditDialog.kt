package app.pwhs.apexfilemanager.features.network.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.pwhs.apexfilemanager.core.storage.domain.model.NetworkServer
import app.pwhs.apexfilemanager.core.storage.domain.model.ServerProtocol
import app.pwhs.apexfilemanager.features.network.R
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerEditDialog(
    initialServer: NetworkServer?,
    isTesting: Boolean,
    onTestConnection: (NetworkServer) -> Unit,
    onSave: (NetworkServer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialServer?.name ?: "") }
    var protocol by remember { mutableStateOf(initialServer?.protocol ?: ServerProtocol.SMB) }
    var host by remember { mutableStateOf(initialServer?.host ?: "") }
    var port by remember { mutableIntStateOf(initialServer?.port ?: protocol.defaultPort) }
    var username by remember { mutableStateOf(initialServer?.username ?: "") }
    var password by remember { mutableStateOf(initialServer?.password ?: "") }
    var isAnonymous by remember { mutableStateOf(initialServer?.isAnonymous ?: false) }
    var shareOrPath by remember { mutableStateOf(initialServer?.shareOrPath ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val currentConfig = NetworkServer(
        id = initialServer?.id ?: UUID.randomUUID().toString(),
        name = name.ifBlank { host },
        protocol = protocol,
        host = host.trim(),
        port = port,
        username = username.trim(),
        password = password,
        isAnonymous = isAnonymous,
        shareOrPath = shareOrPath.trim()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialServer == null) {
                    stringResource(R.string.network_add_server)
                } else {
                    stringResource(R.string.network_edit_server)
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.network_server_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = protocol.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.network_protocol)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        ServerProtocol.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.displayName) },
                                onClick = {
                                    protocol = p
                                    port = p.defaultPort
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.network_host)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = port.toString(),
                    onValueChange = { port = it.toIntOrNull() ?: protocol.defaultPort },
                    label = { Text(stringResource(R.string.network_port)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAnonymous = !isAnonymous },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it }
                    )
                    Text(
                        text = stringResource(R.string.network_anonymous),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (!isAnonymous) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(R.string.network_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.network_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = shareOrPath,
                    onValueChange = { shareOrPath = it },
                    label = {
                        Text(
                            if (protocol == ServerProtocol.SMB) {
                                "Share Name (vd: public, shared)"
                            } else {
                                "Remote Path (vd: /home/user)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { onTestConnection(currentConfig) },
                    enabled = host.isNotBlank() && !isTesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isTesting) {
                            stringResource(R.string.network_testing)
                        } else {
                            stringResource(R.string.network_test_connection)
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentConfig) },
                enabled = host.isNotBlank() && !isTesting
            ) {
                Text(stringResource(R.string.network_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.network_cancel))
            }
        }
    )
}
