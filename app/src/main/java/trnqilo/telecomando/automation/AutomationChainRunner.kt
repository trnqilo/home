package trnqilo.telecomando.automation

import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.execution.CommandExecutionContext

internal class AutomationChainRunner(
  private val commandRunner: AutomationCommandRunner = AutomationCommandRunner(),
) {
  suspend fun execute(
    rule: AutomationRuleWithSteps,
    resolveCommand: suspend (Int) -> CommandWithServers?,
  ): AutomationExecutionResult {
    val steps = rule.steps.sortedBy { it.orderIndex }
    if (steps.isEmpty()) {
      return AutomationExecutionResult(
        ruleId = rule.rule.ruleId,
        ruleName = rule.rule.name,
        steps = emptyList(),
      )
    }

    val results = mutableListOf<AutomationStepExecutionResult>()
    var context = CommandExecutionContext()
    var currentStep = steps.firstOrNull()
    var guard = 0
    val stepsById = steps.associateBy { it.stepId }

    while (currentStep != null && guard <= steps.size) {
      guard++
      val step = currentStep ?: break
      val command = resolveCommand(step.commandId)
      val commandResult = if (command == null) {
        missingCommandResult(step.commandId)
      } else {
        commandRunner.execute(command, context)
      }
      val branchMode = step.branchMode()
      val nextStepId = if (commandResult.succeeded) step.successNextStepId else step.failureNextStepId
      results += AutomationStepExecutionResult(
        index = step.orderIndex,
        branchMode = branchMode,
        command = commandResult,
        nextStepId = nextStepId,
      )
      context = CommandExecutionContext(upstream = commandResult.summary)
      val nextStep = nextStepId?.let(stepsById::get)
      if (nextStep == null || nextStep.orderIndex <= step.orderIndex) {
        break
      }
      currentStep = nextStep
    }

    return AutomationExecutionResult(
      ruleId = rule.rule.ruleId,
      ruleName = rule.rule.name,
      steps = results,
    )
  }
}

private fun missingCommandResult(commandId: Int): AutomationCommandResult =
  AutomationCommandResult(
    commandId = commandId,
    commandName = "Unknown command",
    commandType = "UNKNOWN",
    summary = trnqilo.telecomando.execution.CommandOutputSummary(
      commandName = "Unknown command",
      commandType = "UNKNOWN",
      succeeded = false,
      result = "",
      stdout = "",
      stderr = "Command $commandId could not be resolved.",
      exitCode = null,
      httpStatus = null,
      responseBody = null,
    ),
    targets = emptyList(),
  )
