package trnqilo.telecomando.ui.automations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.automation.AutomationInitiatorDraft
import trnqilo.telecomando.automation.AutomationInitiatorType
import trnqilo.telecomando.automation.AutomationRuleDraft
import trnqilo.telecomando.automation.AutomationStepDraft
import trnqilo.telecomando.automation.addInitiator
import trnqilo.telecomando.automation.addStep
import trnqilo.telecomando.automation.branchMode
import trnqilo.telecomando.automation.moveStep
import trnqilo.telecomando.automation.removeInitiator
import trnqilo.telecomando.automation.removeStep
import trnqilo.telecomando.automation.toDraft
import trnqilo.telecomando.automation.toExecutionPreview
import trnqilo.telecomando.automation.toSave
import trnqilo.telecomando.automation.updateInitiator
import trnqilo.telecomando.automation.updateStep
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.FormSection

@Composable
fun AutomationEditor(
  commands: List<CommandWithServers>,
  model: AutomationRuleWithSteps? = null,
  onSave: (trnqilo.telecomando.automation.AutomationSave) -> Unit,
  onBack: () -> Unit,
) {
  var draft by remember(model?.rule?.ruleId) {
    mutableStateOf(model?.toDraft() ?: AutomationRuleDraft())
  }
  val coroutineScope = rememberCoroutineScope()
  val executionStateHolder = remember(coroutineScope) {
    AutomationExecutionStateHolder(coroutineScope)
  }
  val executionState by executionStateHolder.state.collectAsState()
  DisposableEffect(executionStateHolder) {
    onDispose(executionStateHolder::cancel)
  }
  val commandNamesById = commands.associate { it.command.commandId to it.command.name }
  val canSave = draft.canSave()
  val previewRule = if (canSave) draft.toExecutionPreview() else null

  AppScaffold(
    title = if (model == null) "New automation" else "Edit automation",
    onBack = onBack,
    onSave = { onSave(draft.toSave()) },
    saveEnabled = canSave,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      FormSection("General") {
        OutlinedTextField(
          value = draft.name,
          onValueChange = { draft = draft.copy(name = it) },
          label = { Text("Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        Column {
          Text("Enabled")
          Switch(
            checked = draft.enabled,
            onCheckedChange = { draft = draft.copy(enabled = it) },
          )
        }
      }

      FormSection("Initiators") {
        Text(
          "Add one or more initiators. Each one can have its own settings.",
          color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          draft.initiators.forEachIndexed { index, initiator ->
            InitiatorCard(
              index = index,
              initiator = initiator,
              onInitiatorChange = { update -> draft = draft.updateInitiator(initiator.key, update) },
              onRemove = { draft = draft.removeInitiator(initiator.key) },
            )
          }
        }
        OutlinedButton(
          onClick = { draft = draft.addInitiator() },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Default.Add, contentDescription = null)
          Text("Add initiator")
        }
      }

      FormSection("Steps") {
        Text(
          "Add commands in order, then wire the next step for success, failure, or both.",
          color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          draft.steps.forEachIndexed { index, step ->
            AutomationStepCard(
              index = index,
              step = step,
              commands = commands,
              commandNamesById = commandNamesById,
              steps = draft.steps,
              onStepChange = { update -> draft = draft.updateStep(step.key, update) },
              onMoveUp = { draft = draft.moveStep(step.key, -1) },
              onMoveDown = { draft = draft.moveStep(step.key, 1) },
              onRemove = { draft = draft.removeStep(step.key) },
            )
          }
        }
        OutlinedButton(
          onClick = { draft = draft.addStep() },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Default.Add, contentDescription = null)
          Text("Add step")
        }
      }

      FormSection("Execution") {
        AutomationExecutionPanel(
          ruleName = draft.name.ifBlank { "Automation" },
          canRun = canSave,
          state = executionState,
          onRun = {
            previewRule?.let { rule ->
              executionStateHolder.run(rule) { commandId ->
                commands.firstOrNull { it.command.commandId == commandId }
              }
            }
          },
          onCancel = executionStateHolder::cancel,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun InitiatorCard(
  index: Int,
  initiator: AutomationInitiatorDraft,
  onInitiatorChange: (AutomationInitiatorDraft.() -> AutomationInitiatorDraft) -> Unit,
  onRemove: () -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Initiator ${index + 1}")
      OutlinedButton(onClick = onRemove, enabled = true) {
        Icon(Default.Delete, contentDescription = null)
      }
    }

    InitiatorTypeSelector(
      selectedType = initiator.type,
      onSelected = { type -> onInitiatorChange { copy(type = type) } },
    )

    when (initiator.type) {
      AutomationInitiatorType.Alarm.name -> {
        OutlinedTextField(
          value = initiator.alarmDelayMinutes,
          onValueChange = { onInitiatorChange { copy(alarmDelayMinutes = it.filter(Char::isDigit)) } },
          label = { Text("Delay minutes") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        Text(
          "This initiator fires after the delay.",
          color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      AutomationInitiatorType.SmsReceived.name -> {
        Text(
          "This initiator fires when an SMS arrives.",
          color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      else -> {
        Text(
          "Unsupported initiator type.",
          color = androidx.compose.material3.MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun AutomationStepCard(
  index: Int,
  step: AutomationStepDraft,
  commands: List<CommandWithServers>,
  commandNamesById: Map<Int, String>,
  steps: List<AutomationStepDraft>,
  onStepChange: (AutomationStepDraft.() -> AutomationStepDraft) -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onRemove: () -> Unit,
) {
  val laterSteps = steps.drop(index + 1)
  val branchOptions = listOf<Long?>(null) + laterSteps.map { it.key }
  val branchLabelByKey = buildMap<Long?, String> {
    put(null, "End")
    laterSteps.forEachIndexed { laterIndex, laterStep ->
      val commandName = laterStep.commandId?.let(commandNamesById::get) ?: "Unselected"
      put(laterStep.key, "Step ${index + laterIndex + 2}: $commandName")
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Step ${index + 1}")
      OutlinedButton(onClick = onMoveUp, enabled = index > 0) {
        Icon(Default.KeyboardArrowUp, contentDescription = null)
      }
      OutlinedButton(onClick = onMoveDown, enabled = index < steps.lastIndex) {
        Icon(Default.KeyboardArrowDown, contentDescription = null)
      }
      OutlinedButton(onClick = onRemove, enabled = true) {
        Icon(Default.Delete, contentDescription = null)
      }
    }

    Text(
      "Mode: ${step.branchMode().name.lowercase()}",
      color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
    )

    CommandDropdown(
      label = "Command",
      options = commands.map { it.command.commandId to it.command.name },
      selectedId = step.commandId,
      onSelected = { commandId -> onStepChange { copy(commandId = commandId) } },
    )

    BranchDropdown(
      label = "Continue on success",
      selectedKey = step.successNextKey,
      options = branchOptions,
      optionLabel = { branchLabelByKey[it].orEmpty() },
      onSelected = { nextKey -> onStepChange { copy(successNextKey = nextKey) } },
    )
    BranchDropdown(
      label = "Continue on failure",
      selectedKey = step.failureNextKey,
      options = branchOptions,
      optionLabel = { branchLabelByKey[it].orEmpty() },
      onSelected = { nextKey -> onStepChange { copy(failureNextKey = nextKey) } },
    )
  }
}

@Composable
private fun InitiatorTypeSelector(
  selectedType: String,
  onSelected: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedLabel = selectedType.toInitiatorLabel()

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(selectedLabel)
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      AutomationInitiatorType.entries.forEach { type ->
        DropdownMenuItem(
          text = { Text(type.name.toInitiatorLabel()) },
          onClick = {
            onSelected(type.name)
            expanded = false
          },
        )
      }
    }
  }
}

@Composable
private fun CommandDropdown(
  label: String,
  options: List<Pair<Int, String>>,
  selectedId: Int?,
  onSelected: (Int?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedName = options.firstOrNull { it.first == selectedId }?.second

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(
      onClick = { expanded = true },
      enabled = options.isNotEmpty(),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(selectedName ?: label)
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option.second) },
          onClick = {
            onSelected(option.first)
            expanded = false
          },
        )
      }
    }
  }
}

@Composable
private fun BranchDropdown(
  label: String,
  selectedKey: Long?,
  options: List<Long?>,
  optionLabel: (Long?) -> String,
  onSelected: (Long?) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedName = optionLabel(selectedKey).ifBlank { label }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(selectedName)
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(optionLabel(option)) },
          onClick = {
            onSelected(option)
            expanded = false
          },
        )
      }
    }
  }
}

private fun AutomationRuleDraft.canSave(): Boolean {
  if (name.isBlank()) return false
  if (initiators.isEmpty()) return false
  if (initiators.any { !it.isValid() }) return false
  if (steps.isEmpty()) return false
  if (steps.any { it.commandId == null }) return false

  val stepIndexByKey = steps.withIndex().associate { (index, step) -> step.key to index }
  return steps.withIndex().all { (index, step) ->
    val successOk = step.successNextKey == null || (stepIndexByKey[step.successNextKey] ?: -1) > index
    val failureOk = step.failureNextKey == null || (stepIndexByKey[step.failureNextKey] ?: -1) > index
    successOk && failureOk
  }
}

private fun AutomationInitiatorDraft.isValid(): Boolean = when (type) {
  AutomationInitiatorType.Alarm.name -> alarmDelayMinutes.toIntOrNull() != null
  AutomationInitiatorType.SmsReceived.name -> true
  else -> false
}

private fun String.toInitiatorLabel(): String = when (this) {
  AutomationInitiatorType.Alarm.name -> "Alarm"
  AutomationInitiatorType.SmsReceived.name -> "SMS received"
  else -> this
}
