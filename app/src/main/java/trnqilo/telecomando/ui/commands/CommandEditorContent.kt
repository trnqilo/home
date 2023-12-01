package trnqilo.telecomando.ui.commands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.commands.CommandDraft
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.HttpMethod
import trnqilo.telecomando.data.parseFlatJsonObject
import trnqilo.telecomando.execution.CommandExecutionState
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.FormSection
import trnqilo.telecomando.ui.executor.CommandExecutionPanel

@Composable
internal fun CommandEditorContent(
  title: String,
  draft: CommandDraft,
  allServers: List<ServerOption>,
  executionState: CommandExecutionState,
  onNameChange: (String) -> Unit,
  onCommandChange: (String) -> Unit,
  onTypeChange: (String) -> Unit,
  onHttpUrlChange: (String) -> Unit,
  onHttpMethodChange: (String) -> Unit,
  onHttpHeadersChange: (String) -> Unit,
  onAddServer: (Int) -> Unit,
  onRemoveServer: (Int) -> Unit,
  onRun: () -> Unit,
  onCancel: () -> Unit,
  onSave: () -> Unit,
  onBack: () -> Unit,
) {
  val isHttp = draft.type == CommandType.HTTP.name
  val headersAreValid = !isHttp || draft.httpHeadersJson.parseFlatJsonObject() != null
  val hasCompatibleConnection = allServers.any { it.id in draft.selectedServerIds }
  val hasRunTarget = hasCompatibleConnection && headersAreValid
  val saveEnabled = canSaveCommand(draft) && (!isHttp || hasCompatibleConnection)

  AppScaffold(
    title = title,
    onBack = onBack,
    onSave = onSave,
    saveEnabled = saveEnabled,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      FormSection("Type") {
        CommandTypeSelector(
          selectedType = draft.type,
          onSelected = onTypeChange,
        )
      }
      FormSection("Details") {
        OutlinedTextField(
          value = draft.name,
          onValueChange = onNameChange,
          label = { Text("Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        if (!isHttp) {
          OutlinedTextField(
            value = draft.command,
            onValueChange = onCommandChange,
            label = { Text("Command") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
          )
        }
      }
      if (isHttp) {
        FormSection("HTTP request") {
          HttpConfigForm(
            url = draft.httpPath,
            method = draft.httpMethod,
            headersJson = draft.httpHeadersJson,
            bodyTemplate = draft.command,
            onUrlChange = onHttpUrlChange,
            onMethodChange = onHttpMethodChange,
            onHeadersChange = onHttpHeadersChange,
            onBodyTemplateChange = onCommandChange,
            headersAreValid = headersAreValid,
          )
        }
      }
      FormSection("Connections") {
        ServerSelector(
          allServers = allServers,
          selectedServerIds = draft.selectedServerIds,
          onAddServer = onAddServer,
          onRemoveServer = onRemoveServer,
        )
      }
      FormSection("Execution") {
        CommandExecutionPanel(
          command = draft.command,
          hasTargets = hasRunTarget,
          state = executionState,
          onRun = onRun,
          onCancel = onCancel,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun CommandTypeSelector(
  selectedType: String,
  onSelected: (String) -> Unit,
) {
  var expanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(
      onClick = { expanded.value = true },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Type: ${selectedType}")
    }
    DropdownMenu(
      expanded = expanded.value,
      onDismissRequest = { expanded.value = false },
    ) {
      CommandType.entries.forEach { type ->
        DropdownMenuItem(
          text = { Text(type.name) },
          onClick = {
            onSelected(type.name)
            expanded.value = false
          },
        )
      }
    }
      Text(
      when (selectedType) {
        CommandType.HTTP.name -> "HTTP commands run the same request against each selected HTTP connection."
        else -> "SSH commands run on selected SSH connections."
      },
      color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun HttpConfigForm(
  url: String,
  method: String,
  headersJson: String,
  bodyTemplate: String,
  onUrlChange: (String) -> Unit,
  onMethodChange: (String) -> Unit,
  onHeadersChange: (String) -> Unit,
  onBodyTemplateChange: (String) -> Unit,
  headersAreValid: Boolean,
) {
  OutlinedTextField(
    value = url,
    onValueChange = onUrlChange,
    label = { Text("Path (optional)") },
    placeholder = { Text("/v1/resource") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
  )
  HttpMethodSelector(method = method, onSelected = onMethodChange)
  OutlinedTextField(
    value = headersJson,
    onValueChange = onHeadersChange,
    label = { Text("Headers JSON") },
    modifier = Modifier.fillMaxWidth(),
    minLines = 4,
    isError = !headersAreValid,
    supportingText = {
      if (!headersAreValid) Text("Enter a JSON object with string keys and values")
    },
    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
  )
  Text(
    "Use JSON like {\"Authorization\":\"Bearer ...\"}.",
    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
  )
  if (method in setOf(HttpMethod.POST.name, HttpMethod.PUT.name, HttpMethod.PATCH.name)) {
    OutlinedTextField(
      value = bodyTemplate,
      onValueChange = onBodyTemplateChange,
      label = { Text("Body template") },
      modifier = Modifier.fillMaxWidth(),
      minLines = 3,
      textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
    )
    Text(
      "This body is sent after template substitution.",
      color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

internal fun canSaveCommand(draft: CommandDraft): Boolean = when (draft.type) {
  CommandType.HTTP.name -> draft.name.isNotBlank() &&
    draft.httpHeadersJson.parseFlatJsonObject() != null
  else -> draft.name.isNotBlank() && draft.command.isNotBlank()
}

@Composable
private fun HttpMethodSelector(
  method: String,
  onSelected: (String) -> Unit,
) {
  var expanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(
      onClick = { expanded.value = true },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text("Method: ${method}")
    }
    DropdownMenu(
      expanded = expanded.value,
      onDismissRequest = { expanded.value = false },
    ) {
      HttpMethod.entries.forEach { candidate ->
        DropdownMenuItem(
          text = { Text(candidate.name) },
          onClick = {
            onSelected(candidate.name)
            expanded.value = false
          },
        )
      }
    }
  }
}
