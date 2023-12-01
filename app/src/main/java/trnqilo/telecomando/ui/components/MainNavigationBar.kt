package trnqilo.telecomando.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

private data class MainDestination(
  val route: String,
  val label: String,
  val iconText: String,
)

private val MainDestinations = listOf(
  MainDestination("commands", "Commands", ">_"),
  MainDestination("connections", "Connections", "↔"),
  MainDestination("automations", "Automations", "A"),
)

@Composable
fun MainNavigationBar(
  selectedRoute: String,
  onNavigate: (String) -> Unit,
) {
  NavigationBar {
    MainDestinations.forEach { destination ->
      NavigationBarItem(
        selected = destination.route == selectedRoute,
        onClick = { onNavigate(destination.route) },
        icon = { Text(destination.iconText) },
        label = { Text(destination.label) },
      )
    }
  }
}
