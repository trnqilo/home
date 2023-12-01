package trnqilo.telecomando.ui.executor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.execution.CommandExecutionState

@Composable
fun CommandExecutionPanel(
  command: String,
  hasTargets: Boolean,
  state: CommandExecutionState,
  onRun: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (state.isRunning) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
      ) {
        Text("Cancel execution")
      }
    } else {
      Button(
        onClick = onRun,
        enabled = canRunCommand(command, hasTargets),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Run command")
      }
    }

    if (state.targets.isEmpty()) {
      Text(
        commandExecutionGuidance(command, hasTargets),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
      )
    } else {
      state.targets.forEach { targetState ->
        CommandExecutionResult(targetState)
      }
    }
  }
}

internal fun canRunCommand(command: String, hasTargets: Boolean): Boolean =
  command.isNotBlank() && hasTargets

internal fun commandExecutionGuidance(command: String, hasTargets: Boolean): String = when {
  command.isBlank() -> "Enter a command to run."
  !hasTargets -> "Select at least one server to run this command."
  else -> "Run the command to see output."
}
