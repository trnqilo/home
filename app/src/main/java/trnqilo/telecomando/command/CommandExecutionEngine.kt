package trnqilo.telecomando.command

import kotlinx.coroutines.flow.Flow
import trnqilo.telecomando.execution.CommandExecutionRequest

fun interface CommandExecutionEngine {
  fun execute(request: CommandExecutionRequest): Flow<CommandEvent>
}
