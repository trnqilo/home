package trnqilo.telecomando.ui.automations

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import trnqilo.telecomando.automation.AutomationChainRunner
import trnqilo.telecomando.automation.AutomationExecutionResult
import trnqilo.telecomando.data.CommandWithServers

data class AutomationExecutionState(
  val isRunning: Boolean = false,
  val result: AutomationExecutionResult? = null,
  val error: String? = null,
)

internal class AutomationExecutionStateHolder(
  private val scope: CoroutineScope,
  private val automationRunner: AutomationChainRunner = AutomationChainRunner(),
) {
  private val mutableState = MutableStateFlow(AutomationExecutionState())
  val state: StateFlow<AutomationExecutionState> = mutableState.asStateFlow()

  private var executionJob: Job? = null
  private var executionId = 0

  fun run(
    rule: trnqilo.telecomando.data.AutomationRuleWithSteps,
    resolveCommand: suspend (Int) -> CommandWithServers?,
  ) {
    executionJob?.cancel()
    val currentExecutionId = ++executionId
    mutableState.value = AutomationExecutionState(isRunning = true)

    executionJob = scope.launch {
      try {
        val result = automationRunner.execute(rule, resolveCommand)
        if (currentExecutionId == executionId) {
          mutableState.value = AutomationExecutionState(result = result)
        }
      } catch (exception: CancellationException) {
        if (currentExecutionId == executionId) {
          mutableState.value = AutomationExecutionState()
        }
        throw exception
      } catch (exception: Exception) {
        if (currentExecutionId == executionId) {
          mutableState.value = AutomationExecutionState(error = exception.message ?: "Automation execution failed.")
        }
      } finally {
        if (currentExecutionId == executionId) {
          mutableState.value = mutableState.value.copy(isRunning = false)
          executionJob = null
        }
      }
    }
  }

  fun cancel() {
    executionJob?.cancel()
  }
}
