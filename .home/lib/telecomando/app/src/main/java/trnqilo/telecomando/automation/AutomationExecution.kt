package trnqilo.telecomando.automation

import trnqilo.telecomando.execution.CommandOutputSummary
import trnqilo.telecomando.execution.TargetExecutionState

data class AutomationCommandTargetResult(
  val targetName: String,
  val address: String,
  val status: String,
  val exitCode: Int?,
  val stdout: String,
  val stderr: String,
  val error: String?,
)

data class AutomationCommandResult(
  val commandId: Int,
  val commandName: String,
  val commandType: String,
  val summary: CommandOutputSummary,
  val targets: List<AutomationCommandTargetResult>,
) {
  val succeeded: Boolean
    get() = summary.succeeded

  val summaryStatus: String
    get() = if (succeeded) "success" else "failure"
}

data class AutomationStepExecutionResult(
  val index: Int,
  val branchMode: AutomationStepBranchMode,
  val command: AutomationCommandResult,
  val nextStepId: Int?,
)

data class AutomationExecutionResult(
  val ruleId: Int,
  val ruleName: String,
  val steps: List<AutomationStepExecutionResult>,
)

internal fun TargetExecutionState.toAutomationTargetResult(address: String): AutomationCommandTargetResult =
  AutomationCommandTargetResult(
    targetName = targetName,
    address = address,
    status = status.name.lowercase(),
    exitCode = exitCode,
    stdout = stdout,
    stderr = stderr,
    error = error,
  )
