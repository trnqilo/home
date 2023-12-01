package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ConnectionType {
  SSH,
  HTTP,
}

data class SshConnectionConfig(
  val address: String,
  val port: Int = 22,
  val username: String,
)

data class HttpConnectionConfig(
  val baseUrl: String,
  val defaultHeadersJson: String = "{}",
)

data class ConnectionCredentials(
  val password: String = "",
  val privateKey: String = "",
)

@Entity(tableName = "connections")
data class ConnectionEntity(
  val name: String,
  val type: String,
  val configJson: String,
  val credentialJson: String = "{}",
  @PrimaryKey(autoGenerate = true) val connectionId: Int = 0,
) {
  constructor(
    name: String,
    address: String,
    port: Int = 22,
    username: String,
    password: String,
    key: String,
    serverId: Int = 0,
  ) : this(
    name = name,
    type = ConnectionType.SSH.name,
    configJson = SshConnectionConfig(address, port, username).toJson(),
    credentialJson = ConnectionCredentials(password, key).toJson(),
    connectionId = serverId,
  )

  val serverId: Int get() = connectionId
  val address: String get() = configJson.toSshConnectionConfig().address
  val port: Int get() = configJson.toSshConnectionConfig().port
  val username: String get() = configJson.toSshConnectionConfig().username
  val password: String get() = credentialJson.toConnectionCredentials().password
  val key: String get() = credentialJson.toConnectionCredentials().privateKey

  companion object {
    fun http(
      name: String,
      baseUrl: String,
      defaultHeadersJson: String = "{}",
      connectionId: Int = 0,
    ) = ConnectionEntity(
      name = name,
      type = ConnectionType.HTTP.name,
      configJson = HttpConnectionConfig(baseUrl, defaultHeadersJson).toJson(),
      connectionId = connectionId,
    )
  }
}

internal fun SshConnectionConfig.toJson(): String = flatJsonObjectOf(
  "address" to address,
  "port" to port.toString(),
  "username" to username,
)

internal fun String.toSshConnectionConfig(): SshConnectionConfig {
  val json = parseFlatJsonObject().orEmpty()
  return SshConnectionConfig(
    address = json["address"].orEmpty(),
    port = json["port"]?.toIntOrNull() ?: 22,
    username = json["username"].orEmpty(),
  )
}

internal fun HttpConnectionConfig.toJson(): String = flatJsonObjectOf(
  "baseUrl" to baseUrl,
  "defaultHeadersJson" to defaultHeadersJson,
)

internal fun String.toHttpConnectionConfig(): HttpConnectionConfig {
  val json = parseFlatJsonObject().orEmpty()
  return HttpConnectionConfig(
    baseUrl = json["baseUrl"].orEmpty(),
    defaultHeadersJson = json["defaultHeadersJson"].orEmpty().ifBlank { "{}" },
  )
}

internal fun ConnectionCredentials.toJson(): String = flatJsonObjectOf(
  "password" to password,
  "privateKey" to privateKey,
)

internal fun String.toConnectionCredentials(): ConnectionCredentials {
  val json = parseFlatJsonObject().orEmpty()
  return ConnectionCredentials(
    password = json["password"].orEmpty(),
    privateKey = json["privateKey"].orEmpty(),
  )
}

fun interface ConnectionCredentialResolver {
  fun resolve(connection: ConnectionEntity): ConnectionCredentials
}

object PersistedConnectionCredentialResolver : ConnectionCredentialResolver {
  override fun resolve(connection: ConnectionEntity): ConnectionCredentials =
    connection.credentialJson.toConnectionCredentials()
}
