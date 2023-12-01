package trnqilo.telecomando.command

import kotlinx.coroutines.flow.Flow
import trnqilo.telecomando.ssh.SshConnectionRequest

data class SshCommandRequest(
  val command: String,
  val destination: String,
  val port: Int = 22,
  val user: String = "",
  val password: String = "",
)

internal fun SshCommandRequest.toConnectionRequest() = SshConnectionRequest(
  destination = destination,
  port = port,
  user = user,
  password = password,
)

sealed interface CommandEvent {
  data class Stdout(val text: String) : CommandEvent
  data class Stderr(val text: String) : CommandEvent
  data class Completed(val exitCode: Int) : CommandEvent
  data class Failed(val message: String) : CommandEvent
}

fun interface CommandEventStream {
  fun execute(request: SshCommandRequest): Flow<CommandEvent>
}
