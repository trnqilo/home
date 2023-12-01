package trnqilo.telecomando.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainAppBar(
  title: String = "Telecomando",
  onBack: (() -> Unit)? = null,
  onSave: (() -> Unit)? = null,
  saveEnabled: Boolean = true,
  onSettings: (() -> Unit)? = null,
) {
  TopAppBar(
    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    colors = topAppBarColors(),
    actions = {
      if (onSave != null && onBack != null) {
        IconButton(enabled = saveEnabled, onClick = {
          onSave()
          onBack()
        }) {
          Icon(Filled.Send, contentDescription = "Save")
        }
      }
      if (onSettings != null) {
        IconButton(onClick = onSettings) {
          Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
      }
    },
    navigationIcon = {
      if (onBack != null) {
        IconButton(onClick = onBack) {
          Icon(Filled.ArrowBack, contentDescription = "Back")
        }
      }
    }
  )
}
