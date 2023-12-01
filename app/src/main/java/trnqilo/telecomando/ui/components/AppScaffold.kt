package trnqilo.telecomando.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun AppScaffold(
  title: String = "Telecomando",
  onBack: (() -> Unit)? = null,
  onSave: (() -> Unit)? = null,
  saveEnabled: Boolean = true,
  onAdd: (() -> Unit)? = null,
  snackState: SnackbarHostState? = null,
  onSettings: (() -> Unit)? = null,
  bottomBar: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    topBar = {
      MainAppBar(
        title = title,
        onBack = onBack,
        onSave = onSave,
        saveEnabled = saveEnabled,
        onSettings = onSettings,
      )
    },
    floatingActionButton = {
      if (onAdd != null) FloatingActionButton(onClick = onAdd) {
        Icon(Default.Add, contentDescription = "Add")
      }
    },
    snackbarHost = {
      snackState?.let { SnackbarHost(hostState = it) }
    },
    bottomBar = bottomBar,
  ) { paddingValues ->
    content(paddingValues)
  }
}
