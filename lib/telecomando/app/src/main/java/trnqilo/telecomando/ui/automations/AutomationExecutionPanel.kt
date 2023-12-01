package trnqilo.telecomando.ui.automations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import trnqilo.telecomando.automation.AutomationExecutionResult
import trnqilo.telecomando.automation.AutomationStepExecutionResult
import trnqilo.telecomando.automation.AutomationStepBranchMode

@Composable
fun AutomationExecutionPanel(
  ruleName: String,
  canRun: Boolean,
  state: AutomationExecutionState,
  onRun: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (state.isRunning) {
      androidx.compose.material3.LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
        enabled = canRun,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Run automation")
      }
    }

    state.error?.let { error ->
      Text(
        error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    when {
      state.isRunning -> {
        Text(
          "Running \"$ruleName\"...",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
      state.result == null -> {
        Text(
          if (canRun) {
            "Run the automation to see step-by-step output."
          } else {
            "Add at least one valid step before running this automation."
          },
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
      else -> {
        AutomationRunResult(result = state.result)
      }
    }
  }
}

@Composable
private fun AutomationRunResult(result: AutomationExecutionResult) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      if (result.steps.isEmpty()) {
        "No steps executed."
      } else {
        if (result.steps.last().command.succeeded) "Automation succeeded" else "Automation failed"
      },
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      style = MaterialTheme.typography.bodyMedium,
    )
    result.steps.forEach { step ->
      AutomationStepResultCard(step)
    }
  }
}

@Composable
private fun AutomationStepResultCard(step: AutomationStepExecutionResult) {
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            "Step ${step.index + 1}",
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            "${step.command.commandName} · ${step.command.commandType}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
        AutomationStatusBadge(step)
      }

      Text(
        "Branch: ${step.branchMode.name.lowercase()}${step.nextStepId?.let { " → step $it" } ?: " → end"}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
      )

      if (step.command.summary.result.isNotBlank()) {
        AutomationOutput("Result", step.command.summary.result)
      }
      if (step.command.summary.stderr.isNotBlank()) {
        AutomationOutput("Standard error", step.command.summary.stderr, MaterialTheme.colorScheme.error)
      }

      step.command.targets.forEach { target ->
        AutomationTargetResultCard(target)
      }
    }
  }
}

@Composable
private fun AutomationTargetResultCard(target: trnqilo.telecomando.automation.AutomationCommandTargetResult) {
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(target.targetName, style = MaterialTheme.typography.titleSmall)
          Text(
            target.address,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
          )
        }
        AutomationTargetStatusBadge(target.status)
      }

      target.error?.let { error ->
        AutomationOutput("Error", error, MaterialTheme.colorScheme.error)
      }
      if (target.stdout.isNotBlank()) {
        AutomationOutput("Standard output", target.stdout)
      }
      if (target.stderr.isNotBlank()) {
        AutomationOutput("Standard error", target.stderr, MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
private fun AutomationOutput(label: String, text: String, color: Color = Color.Unspecified) {
  Spacer(Modifier.height(4.dp))
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
private fun AutomationStatusBadge(step: AutomationStepExecutionResult) {
  val color = when (step.command.summary.succeeded) {
    true -> MaterialTheme.colorScheme.primary
    false -> MaterialTheme.colorScheme.error
  }
  Surface(
    color = color.copy(alpha = 0.14f),
    contentColor = color,
    shape = CircleShape,
  ) {
    Text(
      if (step.command.summary.succeeded) "Succeeded" else "Failed",
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      style = MaterialTheme.typography.labelMedium,
    )
  }
}

@Composable
private fun AutomationTargetStatusBadge(status: String) {
  val color = when (status.lowercase()) {
    "succeeded" -> MaterialTheme.colorScheme.primary
    "failed" -> MaterialTheme.colorScheme.error
    "running" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }
  Surface(
    color = color.copy(alpha = 0.14f),
    contentColor = color,
    shape = CircleShape,
  ) {
    Text(
      status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      style = MaterialTheme.typography.labelMedium,
    )
  }
}
