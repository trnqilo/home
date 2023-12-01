package trnqilo.telecomando.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.preferences.ThemeMode
import trnqilo.telecomando.ui.automations.AutomationEditor
import trnqilo.telecomando.ui.automations.AutomationsList
import trnqilo.telecomando.ui.commands.CommandEditor
import trnqilo.telecomando.ui.commands.CommandsList
import trnqilo.telecomando.ui.components.MainNavigationBar
import trnqilo.telecomando.ui.settings.SettingsScreen
import trnqilo.telecomando.ui.servers.ServerEditor
import trnqilo.telecomando.ui.servers.ServersList
import trnqilo.telecomando.ui.terminal.TerminalScreen

@Composable
fun App(
  appViewModel: AppViewModel,
  appVersionName: String,
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
) {
  val navController = rememberNavController()
  var terminalServer by remember { mutableStateOf<ServerEntity?>(null) }
  val connections by appViewModel.connections.collectAsState(initial = emptyList())
  val commands by appViewModel.commands.collectAsState(initial = emptyList())
  val automations by appViewModel.automations.collectAsState(initial = emptyList())
  val navigateToTopLevel: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  NavHost(navController = navController, startDestination = CommandsRoute) {
    composable("connections") {
      ServersList(
        connections,
        onAdd = { navController.navigate("addConnection") },
        onSelect = { id -> navController.navigate("editConnection/$id") },
        onSettings = {
          navController.navigate(SettingsRoute) {
            launchSingleTop = true
          }
        },
        bottomBar = {
          MainNavigationBar(ConnectionsRoute, navigateToTopLevel)
        },
      )
    }
    composable("addConnection") {
      ServerEditor(appViewModel::addConnection, navController::navigateUp)
    }
    composable(
      "editConnection/{id}",
      arguments = listOf(navArgument("id") { type = IntType })
    ) { backStackEntry ->
      ServerEditor(
        onSave = appViewModel::addConnection,
        onBack = navController::navigateUp,
        onConnect = { server ->
          terminalServer = server
          navController.navigate(TerminalRoute) {
            launchSingleTop = true
          }
        },
        model = connections.first<ServerEntity> {
          it.serverId == (backStackEntry.arguments?.getInt("id")
            ?: return@composable)
        }
      )
    }
    composable("commands") {
      CommandsList(
        commands,
        onAdd = { navController.navigate("addCommand") },
        onSelect = { id -> navController.navigate("editCommand/$id") },
        onSettings = {
          navController.navigate(SettingsRoute) {
            launchSingleTop = true
          }
        },
        bottomBar = {
          MainNavigationBar(CommandsRoute, navigateToTopLevel)
        },
      )
    }
    composable(SettingsRoute) {
      SettingsScreen(
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange,
        appVersionName = appVersionName,
        onBack = navController::navigateUp,
      )
    }
    composable("automations") {
      AutomationsList(
        models = automations,
        commands = commands,
        onAdd = { navController.navigate("addAutomation") },
        onSelect = { id -> navController.navigate("editAutomation/$id") },
        onSettings = {
          navController.navigate(SettingsRoute) {
            launchSingleTop = true
          }
        },
        bottomBar = {
          MainNavigationBar(AutomationsRoute, navigateToTopLevel)
        },
      )
    }
    composable("addAutomation") {
      AutomationEditor(
        commands = commands,
        onSave = appViewModel::addAutomation,
        onBack = navController::navigateUp,
      )
    }
    composable(
      "editAutomation/{id}",
      arguments = listOf(navArgument("id") { type = IntType })
    ) { backStackEntry ->
      val automationId = backStackEntry.arguments?.getInt("id") ?: return@composable
      AutomationEditor(
        commands = commands,
        model = automations.firstOrNull { it.rule.ruleId == automationId }
          ?: return@composable,
        onSave = appViewModel::addAutomation,
        onBack = navController::navigateUp,
      )
    }
    composable(TerminalRoute) {
      val server = terminalServer
      if (server == null) {
        return@composable
      } else {
        DisposableEffect(server.serverId) {
          onDispose {
            terminalServer = null
          }
        }
        TerminalScreen(
          server = server,
          onBack = { navController.popBackStack() },
        )
      }
    }
    composable("addCommand") {
      CommandEditor(appViewModel::addCommand, navController::navigateUp, connections)
    }
    composable(
      "editCommand/{id}",
      arguments = listOf(navArgument("id") { type = IntType })
    ) { backStackEntry ->
      CommandEditor(
        appViewModel::addCommand,
        navController::navigateUp,
        connections,
        commands.first<CommandWithServers> {
          it.command.commandId == (backStackEntry.arguments?.getInt("id")
            ?: return@composable)
        }
      )
    }
  }
}

private const val CommandsRoute = "commands"
private const val ConnectionsRoute = "connections"
private const val AutomationsRoute = "automations"
private const val SettingsRoute = "settings"
private const val TerminalRoute = "terminal"
