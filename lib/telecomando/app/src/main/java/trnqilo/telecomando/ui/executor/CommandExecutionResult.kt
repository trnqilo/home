package trnqilo.telecomando.ui.executor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.execution.ExecutionStatus
import trnqilo.telecomando.execution.TargetExecutionState

@Composable
internal fun CommandExecutionResult(state: TargetExecutionState) {
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(state.targetName, style = MaterialTheme.typography.titleMedium)
        ExecutionStatusBadge(state)
      }

      state.error?.let { error ->
        CommandOutput("Error", error, MaterialTheme.colorScheme.error)
      }
      if (state.stdout.isNotEmpty()) {
        CommandOutput("Standard output", state.stdout)
      }
      if (state.stderr.isNotEmpty()) {
        CommandOutput("Standard error", state.stderr, MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
private fun CommandOutput(label: String, text: String, color: Color = Color.Unspecified) {
  Spacer(Modifier.height(8.dp))
  Text(
    label,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    style = MaterialTheme.typography.labelSmall,
  )
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = MaterialTheme.shapes.small,
  ) {
    SelectionContainer {
      Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(12.dp),
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@Composable
private fun ExecutionStatusBadge(state: TargetExecutionState) {
  val color = when (state.status) {
    ExecutionStatus.Succeeded -> MaterialTheme.colorScheme.primary
    ExecutionStatus.Failed -> MaterialTheme.colorScheme.error
    ExecutionStatus.Running -> MaterialTheme.colorScheme.tertiary
    ExecutionStatus.Pending, ExecutionStatus.Cancelled -> MaterialTheme.colorScheme.onSurfaceVariant
  }
  Surface(
    color = color.copy(alpha = 0.14f),
    contentColor = color,
    shape = CircleShape,
  ) {
    Text(
      executionStatusText(state),
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      style = MaterialTheme.typography.labelMedium,
    )
  }
}

internal fun executionStatusText(state: TargetExecutionState): String = when (state.status) {
  ExecutionStatus.Pending -> "Pending"
  ExecutionStatus.Running -> "Running"
  ExecutionStatus.Succeeded -> "Completed · exit ${state.exitCode ?: 0}"
  ExecutionStatus.Failed -> state.exitCode?.let { "Failed · exit $it" } ?: "Failed"
  ExecutionStatus.Cancelled -> "Cancelled"
}
