package trnqilo.telecomando.ui.servers

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
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.data.ConnectionType
import trnqilo.telecomando.data.toHttpConnectionConfig
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.EmptyState

@Composable
fun ServersList(
  models: List<ServerEntity>,
  onAdd: () -> Unit,
  onSelect: (Int) -> Unit,
  onSettings: () -> Unit,
  bottomBar: @Composable () -> Unit,
) {
  AppScaffold(
    title = "Connections",
    onAdd = onAdd,
    onSettings = onSettings,
    bottomBar = bottomBar,
  ) { paddingValues ->
    if (models.isEmpty()) {
      EmptyState(
        title = "No connections yet",
        description = "Add an SSH or HTTP connection before assigning it to a command.",
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
        items(models, key = ServerEntity::serverId) { model ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelect(model.serverId) },
          ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(model.name, style = MaterialTheme.typography.titleMedium)
              Text(model.type, style = MaterialTheme.typography.labelMedium)
              Text(
                if (model.type == ConnectionType.HTTP.name) {
                  model.configJson.toHttpConnectionConfig().baseUrl
                } else {
                  "${model.username}@${model.address}:${model.port}"
                },
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
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
