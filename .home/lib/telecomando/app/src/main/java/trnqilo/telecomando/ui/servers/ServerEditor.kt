package trnqilo.telecomando.ui.servers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.data.ConnectionType
import trnqilo.telecomando.data.toHttpConnectionConfig
import trnqilo.telecomando.data.parseFlatJsonObject
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.FormSection

@Composable
fun ServerEditor(
  onSave: (ServerEntity) -> Unit,
  onBack: () -> Unit,
  onConnect: (ServerEntity) -> Unit = {},
  model: ServerEntity? = null,
) {
  var connectionType by remember { mutableStateOf(model?.type ?: ConnectionType.SSH.name) }
  var typeExpanded by remember { mutableStateOf(false) }
  var name by remember { mutableStateOf(model?.name ?: "") }
  var address by remember { mutableStateOf(model?.address ?: "") }
  var port by remember { mutableStateOf(model?.port?.toString() ?: "22") }
  var username by remember { mutableStateOf(model?.username ?: "") }
  var password by remember { mutableStateOf(model?.password ?: "") }
  var key by remember { mutableStateOf(model?.key ?: "") }
  val initialHttpConfig = remember(model?.connectionId) {
    model?.takeIf { it.type == ConnectionType.HTTP.name }?.configJson?.toHttpConnectionConfig()
  }
  var baseUrl by remember { mutableStateOf(initialHttpConfig?.baseUrl ?: "") }
  var defaultHeadersJson by remember { mutableStateOf(initialHttpConfig?.defaultHeadersJson ?: "{}") }
  val portNumber = port.toIntOrNull()
  val portIsValid = portNumber != null && portNumber in 1..65535
  val defaultHeadersAreValid = defaultHeadersJson.parseFlatJsonObject() != null
  val canSave = name.isNotBlank() && when (connectionType) {
    ConnectionType.HTTP.name -> baseUrl.isNotBlank() && defaultHeadersAreValid
    else -> address.isNotBlank() && username.isNotBlank() && portIsValid
  }
  val canConnect = model != null && connectionType == ConnectionType.SSH.name &&
    address.isNotBlank() && username.isNotBlank() && portIsValid
  val buildCurrentServer = {
    if (connectionType == ConnectionType.HTTP.name) {
      ServerEntity.http(
        name = name,
        baseUrl = baseUrl,
        defaultHeadersJson = defaultHeadersJson.ifBlank { "{}" },
        connectionId = model?.connectionId ?: 0,
      )
    } else {
      ServerEntity(
        name = name,
        address = address,
        port = requireNotNull(portNumber),
        username = username,
        password = password,
        key = key,
        serverId = model?.serverId ?: 0,
      )
    }
  }

  AppScaffold(
    title = if (model == null) "Add connection" else "Edit connection",
    onBack = onBack,
    saveEnabled = canSave,
    onSave = { onSave(buildCurrentServer()) },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      FormSection("Connection") {
        if (model == null) {
          OutlinedButton(
            onClick = { typeExpanded = true },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Type: $connectionType")
          }
          DropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = { typeExpanded = false },
          ) {
            ConnectionType.entries.forEach { type ->
              DropdownMenuItem(
                text = { Text(type.name) },
                onClick = {
                  connectionType = type.name
                  typeExpanded = false
                },
              )
            }
          }
        } else {
          Text("Type: $connectionType", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        if (connectionType == ConnectionType.HTTP.name) {
          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL") },
            placeholder = { Text("https://api.example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          OutlinedTextField(
            value = defaultHeadersJson,
            onValueChange = { defaultHeadersJson = it },
            label = { Text("Default headers JSON") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            isError = !defaultHeadersAreValid,
            supportingText = {
              if (!defaultHeadersAreValid) Text("Enter a JSON object with string keys and values")
            },
          )
          Text(
            "Defaults apply to every request. Do not store authentication secrets here.",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Host") },
            placeholder = { Text("127.0.0.1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          OutlinedTextField(
            value = port,
            onValueChange = { value -> if (value.isEmpty() || value.all(Char::isDigit)) port = value },
            label = { Text("Port") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("server-port"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = !portIsValid,
            supportingText = {
              if (!portIsValid) Text("Enter a port from 1 to 65535")
            },
            singleLine = true,
          )
        }
      }
      if (connectionType == ConnectionType.SSH.name) FormSection("Authentication") {
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
        )
        OutlinedTextField(
          value = key,
          onValueChange = { key = it },
          label = { Text("Private key") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
        )
        if (model != null) {
          Button(
            onClick = { onConnect(buildCurrentServer()) },
            enabled = canConnect,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Connect")
          }
        }
      }
    }
  }
}
