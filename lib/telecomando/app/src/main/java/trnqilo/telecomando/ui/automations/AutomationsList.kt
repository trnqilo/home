package trnqilo.telecomando.ui.automations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.parseFlatJsonObject
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.EmptyState

@Composable
fun AutomationsList(
  models: List<AutomationRuleWithSteps>,
  commands: List<CommandWithServers>,
  onAdd: () -> Unit,
  onSelect: (Int) -> Unit,
  onSettings: () -> Unit,
  bottomBar: @Composable () -> Unit,
) {
  val commandNamesById = commands.associate { it.command.commandId to it.command.name }
  AppScaffold(
    title = "Automations",
    onAdd = onAdd,
    onSettings = onSettings,
    bottomBar = bottomBar,
  ) { paddingValues ->
    if (models.isEmpty()) {
      EmptyState(
        title = "No automations yet",
        description = "Create initiators and chain commands behind them.",
        modifier = Modifier.padding(paddingValues),
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(models, key = { it.rule.ruleId }) { model ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelect(model.rule.ruleId) },
          ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(model.rule.name, style = MaterialTheme.typography.titleMedium)
              Text(
                if (model.rule.enabled) "Enabled" else "Disabled",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
              )
              Text(
                model.initiatorSummary(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                model.chainSummary(commandNamesById),
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
        }
      }
    }
  }
}

private fun AutomationRuleWithSteps.initiatorSummary(): String =
  if (initiators.isEmpty()) {
    "Initiators: none"
  } else {
    val summary = initiators.sortedBy { it.orderIndex }.joinToString(", ") { initiator ->
      when (initiator.type) {
        "Alarm" -> "alarm in ${
          initiator.configJson.takeIf { it.isNotBlank() }
            ?.parseFlatJsonObject()
            ?.get("delayMinutes")
            .orEmpty()
            .ifBlank { "15" }
        } minute(s)"
        "SmsReceived" -> "SMS received"
        else -> initiator.type
      }
    }
    "Initiators: $summary"
  }

private fun AutomationRuleWithSteps.chainSummary(commandNamesById: Map<Int, String>): String {
  if (steps.isEmpty()) return "Chain: no steps"
  val orderedSteps = steps.sortedBy { it.orderIndex }
  val names = orderedSteps.map { step ->
    commandNamesById[step.commandId] ?: "Unknown command"
  }
  val visible = names.take(3).joinToString(" → ")
  val suffix = if (names.size > 3) " → … (${names.size} steps)" else " (${names.size} step${if (names.size == 1) "" else "s"})"
  return "Chain: $visible$suffix"
}
