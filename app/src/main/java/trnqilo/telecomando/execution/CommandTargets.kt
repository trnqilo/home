package trnqilo.telecomando.execution

import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ConnectionType
import trnqilo.telecomando.data.PersistedConnectionCredentialResolver
import trnqilo.telecomando.data.toHttpConnectionConfig
import trnqilo.telecomando.data.toSshConnectionConfig
import trnqilo.telecomando.data.toCommandType
import trnqilo.telecomando.data.toHttpCommandConfig

internal fun CommandWithServers.toExecutionTargets(): List<CommandTarget> = when (command.type.toCommandType()) {
  CommandType.SSH -> servers.filter { it.type == ConnectionType.SSH.name }.map { connection ->
    val config = connection.configJson.toSshConnectionConfig()
    val credentials = PersistedConnectionCredentialResolver.resolve(connection)
    SshTarget(
      id = connection.connectionId,
      name = connection.name,
      address = config.address,
      port = config.port,
      username = config.username,
      password = credentials.password,
    )
  }
  CommandType.HTTP -> servers.filter { it.type == ConnectionType.HTTP.name }.map { connection ->
    val config = connection.configJson.toHttpConnectionConfig()
    HttpTarget(
      id = connection.connectionId,
      name = connection.name,
      baseUrl = config.baseUrl,
      defaultHeadersJson = config.defaultHeadersJson,
    )
  }
}
