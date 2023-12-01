package trnqilo.telecomando.commands

import trnqilo.telecomando.data.CommandType

data class CommandDraft(
  val name: String = "",
  val command: String = "",
  val selectedServerIds: List<Int> = emptyList(),
  val type: String = CommandType.SSH.name,
  val httpPath: String = "",
  val httpMethod: String = "GET",
  val httpHeadersJson: String = "{}",
)
