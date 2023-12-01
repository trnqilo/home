package trnqilo.telecomando.execution

import trnqilo.telecomando.data.CommandWithServers

sealed interface CommandTarget {
  val id: Int
  val name: String
}

data class SshTarget(
  override val id: Int,
  override val name: String,
  val address: String,
  val port: Int = 22,
  val username: String,
  val password: String,
) : CommandTarget

data class HttpTarget(
  override val id: Int,
  override val name: String,
  val baseUrl: String,
  val defaultHeadersJson: String = "{}",
) : CommandTarget

data class CommandExecutionContext(
  val upstream: CommandOutputSummary? = null,
)

data class CommandOutputSummary(
  val commandName: String,
  val commandType: String,
  val succeeded: Boolean,
  val result: String,
  val stdout: String,
  val stderr: String,
  val exitCode: Int?,
  val httpStatus: Int? = null,
  val responseBody: String? = null,
) {
  val primaryOutput: String
    get() = result
}

data class CommandExecutionRequest(
  val command: CommandWithServers,
  val targets: List<CommandTarget>,
  val context: CommandExecutionContext = CommandExecutionContext(),
)

enum class ExecutionStatus {
  Pending,
  Running,
  Succeeded,
  Failed,
  Cancelled,
}

data class TargetExecutionState(
  val targetId: Int,
  val targetName: String,
  val status: ExecutionStatus = ExecutionStatus.Pending,
  val stdout: String = "",
  val stderr: String = "",
  val exitCode: Int? = null,
  val error: String? = null,
)

data class CommandExecutionState(
  val targets: List<TargetExecutionState> = emptyList(),
  val isRunning: Boolean = false,
)
