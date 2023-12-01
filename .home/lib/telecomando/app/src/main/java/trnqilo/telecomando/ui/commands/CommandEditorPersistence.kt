package trnqilo.telecomando.ui.commands

import trnqilo.telecomando.commands.CommandDraft
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.HttpMethod
import trnqilo.telecomando.data.HttpCommandConfig
import trnqilo.telecomando.data.toHttpCommandConfig
import trnqilo.telecomando.data.toJson
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.execution.CommandTarget
import trnqilo.telecomando.execution.HttpTarget
import trnqilo.telecomando.execution.SshTarget
import trnqilo.telecomando.data.ConnectionType
import trnqilo.telecomando.data.toHttpConnectionConfig

internal data class CommandSave(
  val command: CommandWithServers,
  val removedServers: List<ServerEntity>,
)

internal fun CommandWithServers?.toCommandDraft(): CommandDraft {
  if (this == null) return CommandDraft()
  val relatedServerIds = servers.mapTo(mutableSetOf(), ServerEntity::serverId)
  val orderedServerIds = command.serverIds
    .split(',')
    .mapNotNull(String::toIntOrNull)
    .filter(relatedServerIds::contains)
  val httpConfig = command.configJson.takeIf { it.isNotBlank() }?.toHttpCommandConfig()

  return CommandDraft(
    name = command.name,
    command = command.command,
    selectedServerIds = orderedServerIds,
    type = command.type,
    httpPath = httpConfig?.path.orEmpty(),
    httpMethod = httpConfig?.method ?: HttpMethod.GET.name,
    httpHeadersJson = httpConfig?.headersJson ?: "{}",
  )
}

internal fun CommandDraft.toCommandSave(
  original: CommandWithServers?,
  allServers: List<ServerEntity>,
): CommandSave {
  val preview = toCommandPreview(allServers, original?.command?.commandId ?: 0)
  val removedServers = original?.servers.orEmpty().filter { connection ->
    connection.connectionId !in selectedServerIds
  }
  return CommandSave(preview, removedServers)
}

private fun ServerEntity.serverIdString(): String = serverId.toString()

internal fun CommandDraft.toCommandPreview(
  allServers: List<ServerEntity>,
  commandId: Int = 0,
): CommandWithServers {
  val serversById = allServers.associateBy(ServerEntity::serverId)
  val requiredConnectionType = when (type) {
    CommandType.HTTP.name -> ConnectionType.HTTP.name
    else -> ConnectionType.SSH.name
  }
  val selectedServers = selectedServerIds.mapNotNull(serversById::get)
    .filter { it.type == requiredConnectionType }
  val configJson = when (type) {
    CommandType.HTTP.name -> HttpCommandConfig(
      path = httpPath,
      method = httpMethod,
      headersJson = httpHeadersJson.ifBlank { "{}" },
    ).toJson()
    else -> "{}"
  }
  val entity = CommandEntity(
    name = name,
    command = command,
    serverIds = selectedServers.joinToString(",", transform = ServerEntity::serverIdString),
    type = type,
    configJson = configJson,
    commandId = commandId,
  )
  return CommandWithServers(entity, selectedServers)
}

internal fun CommandDraft.toExecutionTargets(
  allServers: List<ServerEntity>,
): List<CommandTarget> {
  val serversById = allServers.associateBy(ServerEntity::serverId)
  return when (type) {
    CommandType.HTTP.name -> selectedServerIds.mapNotNull(serversById::get)
      .filter { it.type == ConnectionType.HTTP.name }
      .map { connection ->
        val config = connection.configJson.toHttpConnectionConfig()
        HttpTarget(
          id = connection.connectionId,
          name = connection.name,
          baseUrl = config.baseUrl,
          defaultHeadersJson = config.defaultHeadersJson,
        )
      }
    else -> selectedServerIds.mapNotNull(serversById::get)
      .filter { it.type == ConnectionType.SSH.name }
      .map { server ->
      SshTarget(
        id = server.serverId,
        name = server.name,
        address = server.address,
        port = server.port,
        username = server.username,
        password = server.password,
      )
    }
  }
}
