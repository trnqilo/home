package trnqilo.telecomando.automation

import trnqilo.telecomando.command.CommandRunner
import trnqilo.telecomando.command.CommandExecutionEngine
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.toCommandType
import trnqilo.telecomando.execution.CommandExecutionContext
import trnqilo.telecomando.execution.CommandExecutionReducer
import trnqilo.telecomando.execution.CommandExecutionRequest
import trnqilo.telecomando.execution.CommandOutputSummary
import trnqilo.telecomando.execution.TargetExecutionState
import trnqilo.telecomando.execution.CommandExecutionState
import trnqilo.telecomando.execution.isTerminal
import trnqilo.telecomando.execution.toExecutionTargets
import trnqilo.telecomando.execution.CommandTarget
import trnqilo.telecomando.execution.HttpTarget
import trnqilo.telecomando.execution.SshTarget

internal class AutomationCommandRunner(
  private val commandRunner: CommandExecutionEngine = CommandRunner(),
) {
  suspend fun execute(
    command: CommandWithServers,
    context: CommandExecutionContext = CommandExecutionContext(),
  ): AutomationCommandResult {
    val targets = command.toExecutionTargets()
    val commandType = command.command.type.toCommandType()
    var state = CommandExecutionState(
      targets = targets.map { target -> TargetExecutionState(target.id, target.name) },
      isRunning = true,
    )

    targets.forEachIndexed { index, target ->
      state = CommandExecutionReducer.markRunning(state, index)
      var receivedTerminalEvent = false
      commandRunner.execute(
        CommandExecutionRequest(
          command = command,
          targets = listOf(target),
          context = context,
        )
      ).collect { event ->
        receivedTerminalEvent = receivedTerminalEvent || event.isTerminal
        state = CommandExecutionReducer.applyEvent(state, index, event)
      }
      if (!receivedTerminalEvent) {
        state = CommandExecutionReducer.markMissingResult(state, index)
      }
    }

    state = CommandExecutionReducer.finish(state)
    val targetResults = state.targets.zip(targets) { targetState, target ->
      targetState.toAutomationTargetResult(target.displayAddress())
    }
    val summary = commandOutputSummary(command, commandType, targetResults)
    return AutomationCommandResult(
      commandId = command.command.commandId,
      commandName = command.command.name,
      commandType = commandType.name,
      summary = summary,
      targets = targetResults,
    )
  }
}

private fun CommandTarget.displayAddress(): String = when (this) {
  is SshTarget -> address
  is HttpTarget -> baseUrl
}

private fun commandOutputSummary(
  command: CommandWithServers,
  commandType: CommandType,
  targets: List<AutomationCommandTargetResult>,
): CommandOutputSummary {
  val stdout = targets.joinToString(separator = "\n") { it.stdout }.trimEnd()
  val stderr = targets.joinToString(separator = "\n") { it.stderr }.trimEnd()
  val primaryOutput = when {
    targets.firstOrNull { it.stdout.isNotBlank() } != null ->
      targets.firstOrNull { it.stdout.isNotBlank() }?.stdout.orEmpty()
    targets.firstOrNull { it.stderr.isNotBlank() } != null ->
      targets.firstOrNull { it.stderr.isNotBlank() }?.stderr.orEmpty()
    else -> ""
  }
  val exitCode = targets.firstOrNull { it.exitCode != null }?.exitCode
  val httpStatus = if (commandType == CommandType.HTTP) exitCode else null
  val responseBody = if (commandType == CommandType.HTTP) primaryOutput else null
  return CommandOutputSummary(
    commandName = command.command.name,
    commandType = commandType.name,
    succeeded = targets.isNotEmpty() && targets.all { it.exitCode == 0 && it.error == null },
    result = primaryOutput,
    stdout = stdout,
    stderr = stderr,
    exitCode = exitCode,
    httpStatus = httpStatus,
    responseBody = responseBody,
  )
}
