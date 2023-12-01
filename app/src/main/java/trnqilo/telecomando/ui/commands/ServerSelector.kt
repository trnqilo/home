package trnqilo.telecomando.ui.commands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

internal data class ServerOption(val id: Int, val name: String)

@Composable
internal fun ServerSelector(
  allServers: List<ServerOption>,
  selectedServerIds: List<Int>,
  onAddServer: (Int) -> Unit,
  onRemoveServer: (Int) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val serversById = allServers.associateBy(ServerOption::id)
  val selectedServers = selectedServerIds.mapNotNull(serversById::get)
  val availableServers = allServers.filterNot { it.id in selectedServerIds }

  Box(Modifier.fillMaxWidth()) {
    OutlinedButton(
      onClick = { expanded = true },
      enabled = availableServers.isNotEmpty(),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row {
        Icon(Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add connection")
      }
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      availableServers.forEach { server ->
        DropdownMenuItem(
          text = { Text(server.name) },
          onClick = {
            onAddServer(server.id)
            expanded = false
          },
        )
      }
    }
  }

  if (selectedServers.isEmpty()) {
    Text(
      "No connections selected",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium,
    )
  } else {
    selectedServers.forEach { server ->
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(server.name, style = MaterialTheme.typography.bodyLarge)
          IconButton(onClick = { onRemoveServer(server.id) }) {
            Icon(Default.Delete, contentDescription = "Remove ${server.name}")
          }
        }
      }
    }
  }
}
