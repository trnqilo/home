package trnqilo.telecomando.command

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.toCommandType
import trnqilo.telecomando.execution.CommandExecutionContext
import trnqilo.telecomando.execution.CommandTarget
import trnqilo.telecomando.execution.HttpTarget
import trnqilo.telecomando.execution.SshTarget

class CommandRunner internal constructor(
  private val sshEventStream: CommandEventStream = JschCommandEventStream(),
  private val httpEventStream: HttpCommandEventStream = HttpCommandEventStream(),
) : CommandExecutionEngine {
  override fun execute(request: trnqilo.telecomando.execution.CommandExecutionRequest): Flow<CommandEvent> {
    val commandType = request.command.command.type.toCommandType()
    return when (commandType) {
      CommandType.SSH -> request.targets.firstOrNull()?.let { target ->
        (target as? SshTarget)?.let { executeSsh(request.command, it, request.context) }
      } ?: flowOf(CommandEvent.Failed("SSH command requires at least one target."))
      CommandType.HTTP -> request.targets.firstOrNull()?.let { target ->
        (target as? HttpTarget)?.let { httpEventStream.execute(request.command, it, request.context) }
      } ?: flowOf(CommandEvent.Failed("HTTP command requires at least one target."))
    }
  }

  private fun executeSsh(
    command: CommandWithServers,
    target: SshTarget,
    context: CommandExecutionContext,
  ): Flow<CommandEvent> {
    val request = SshCommandRequest(
      command = command.command.command.renderCommandTemplate(context),
      destination = target.address,
      port = target.port,
      user = target.username,
      password = target.password,
    )
    return sshEventStream.execute(request)
  }
}
