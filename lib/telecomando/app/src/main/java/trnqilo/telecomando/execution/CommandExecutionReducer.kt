package trnqilo.telecomando.execution

import trnqilo.telecomando.command.CommandEvent

internal object CommandExecutionReducer {
  fun start(targets: List<CommandTarget>) = CommandExecutionState(
    targets = targets.map { target -> TargetExecutionState(target.id, target.name) },
    isRunning = true,
  )

  fun markRunning(state: CommandExecutionState, index: Int): CommandExecutionState =
    state.updateTarget(index) { copy(status = ExecutionStatus.Running) }

  fun applyEvent(
    state: CommandExecutionState,
    index: Int,
    event: CommandEvent,
  ): CommandExecutionState = state.updateTarget(index) {
    when (event) {
      is CommandEvent.Stdout -> copy(stdout = stdout + event.text)
      is CommandEvent.Stderr -> copy(stderr = stderr + event.text)
      is CommandEvent.Completed -> copy(
        status = if (event.exitCode == 0) ExecutionStatus.Succeeded else ExecutionStatus.Failed,
        exitCode = event.exitCode,
      )
      is CommandEvent.Failed -> copy(status = ExecutionStatus.Failed, error = event.message)
    }
  }

  fun markMissingResult(state: CommandExecutionState, index: Int): CommandExecutionState =
    state.updateTarget(index) {
      copy(status = ExecutionStatus.Failed, error = "Command ended without a result.")
    }

  fun cancelRunning(state: CommandExecutionState): CommandExecutionState = state.copy(
    targets = state.targets.map { targetState ->
      if (targetState.status == ExecutionStatus.Running) {
        targetState.copy(status = ExecutionStatus.Cancelled)
      } else {
        targetState
      }
    },
    isRunning = false,
  )

  fun finish(state: CommandExecutionState): CommandExecutionState = state.copy(isRunning = false)

  private fun CommandExecutionState.updateTarget(
    index: Int,
    update: TargetExecutionState.() -> TargetExecutionState,
  ): CommandExecutionState = copy(
    targets = targets.mapIndexed { targetIndex, targetState ->
      if (targetIndex == index) targetState.update() else targetState
    }
  )
}

internal val CommandEvent.isTerminal: Boolean
  get() = this is CommandEvent.Completed || this is CommandEvent.Failed
