package trnqilo.telecomando.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import trnqilo.telecomando.preferences.ThemeMode
import trnqilo.telecomando.ui.components.AppScaffold
import trnqilo.telecomando.ui.components.FormSection

@Composable
fun SettingsScreen(
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
  appVersionName: String,
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
  val requestSmsPermission = rememberLauncherForActivityResult(RequestPermission()) { }
  AppScaffold(
    title = "Settings",
    onBack = onBack,
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      FormSection(title = "Appearance") {
        Text(
          "Choose how the app follows your system or overrides it.",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
        Column(Modifier.selectableGroup()) {
          ThemeMode.entries.forEach { mode ->
            ThemeChoiceRow(
              mode = mode,
              selected = mode == themeMode,
              onClick = { onThemeModeChange(mode) },
            )
          }
        }
      }
      FormSection(title = "SMS automations") {
        Text(
          if (hasSmsPermission) {
            "SMS permission is granted."
          } else {
            "Grant SMS permission so SMS-received automations can run."
          },
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
        if (!hasSmsPermission) {
          OutlinedButton(
            onClick = { requestSmsPermission.launch(Manifest.permission.RECEIVE_SMS) },
          ) {
            Text("Grant SMS permission")
          }
        }
      }
      FormSection(title = "About") {
        Text("Telecomando", style = MaterialTheme.typography.titleMedium)
        Text(
          "Version $appVersionName",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
        Text(
          "Run SSH and HTTP commands against reusable connections from a focused, lightweight interface.",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

@Composable
private fun ThemeChoiceRow(
  mode: ThemeMode,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        role = Role.RadioButton,
        onClick = onClick,
      )
      .padding(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(mode.displayName(), style = MaterialTheme.typography.bodyLarge)
      RadioButton(selected = selected, onClick = null)
    }
    Text(
      mode.description(),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

private fun ThemeMode.displayName(): String = when (this) {
  ThemeMode.System -> "System theme"
  ThemeMode.Light -> "Light theme"
  ThemeMode.Dark -> "Dark theme"
}

private fun ThemeMode.description(): String = when (this) {
  ThemeMode.System -> "Use the device-wide appearance setting."
  ThemeMode.Light -> "Always use the light color scheme."
  ThemeMode.Dark -> "Always use the dark color scheme."
}
