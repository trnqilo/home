package trnqilo.telecomando.execution

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import trnqilo.telecomando.command.CommandExecutionEngine

class CommandExecutionStateHolder(
  private val scope: CoroutineScope,
  private val commandRunner: CommandExecutionEngine,
) {
  private val mutableState = MutableStateFlow(CommandExecutionState())
  val state: StateFlow<CommandExecutionState> = mutableState.asStateFlow()

  private var executionJob: Job? = null
  private var executionId = 0

  fun run(request: CommandExecutionRequest) {
    executionJob?.cancel()
    val currentExecutionId = ++executionId
    mutableState.value = CommandExecutionReducer.start(request.targets)

    executionJob = scope.launch {
      try {
        request.targets.forEachIndexed { index, target ->
          mutableState.update { CommandExecutionReducer.markRunning(it, index) }
          var receivedTerminalEvent = false

          commandRunner.execute(request.copy(targets = listOf(target))).collect { event ->
            receivedTerminalEvent = receivedTerminalEvent || event.isTerminal
            mutableState.update { CommandExecutionReducer.applyEvent(it, index, event) }
          }

          if (!receivedTerminalEvent) {
            mutableState.update { CommandExecutionReducer.markMissingResult(it, index) }
          }
        }
      } catch (exception: CancellationException) {
        if (currentExecutionId == executionId) {
          mutableState.update(CommandExecutionReducer::cancelRunning)
        }
        throw exception
      } finally {
        if (currentExecutionId == executionId) {
          mutableState.update(CommandExecutionReducer::finish)
          executionJob = null
        }
      }
    }
  }

  fun cancel() {
    executionJob?.cancel()
  }
}
