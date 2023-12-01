package trnqilo.telecomando.ui.commands

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
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.toHttpCommandConfig
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.EmptyState

@Composable
fun CommandsList(
  models: List<CommandWithServers>,
  onAdd: () -> Unit,
  onSelect: (Int) -> Unit,
  onSettings: () -> Unit,
  bottomBar: @Composable () -> Unit,
) {
  AppScaffold(
    title = "Commands",
    onAdd = onAdd,
    onSettings = onSettings,
    bottomBar = bottomBar,
  ) { paddingValues ->
    if (models.isEmpty()) {
      EmptyState(
        title = "No commands yet",
        description = "Add a command, select compatible connections, and run it.",
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
        items(models, key = { it.command.commandId }) { model ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelect(model.command.commandId) },
          ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(model.command.name, style = MaterialTheme.typography.titleMedium)
              Text(
                when (model.command.type) {
                  CommandType.HTTP.name -> "HTTP"
                  else -> "SSH"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
              )
              Text(
                commandSummary(model),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                model.servers.connectionCountLabel(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
      }
    }
  }
}

private fun List<*>.connectionCountLabel(): String = when (size) {
  0 -> "No connections selected"
  1 -> "1 connection"
  else -> "$size connections"
}

private fun commandSummary(model: CommandWithServers): String = when (model.command.type) {
  CommandType.HTTP.name -> {
    val config = model.command.configJson.takeIf { it.isNotBlank() }?.toHttpCommandConfig()
    val method = config?.method ?: "GET"
    val path = config?.path.orEmpty().ifBlank { "connection base URL" }
    "$method $path"
  }
  else -> model.command.command
}
