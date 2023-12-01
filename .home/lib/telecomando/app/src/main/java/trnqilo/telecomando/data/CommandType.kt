package trnqilo.telecomando.data

enum class CommandType {
  SSH,
  HTTP,
}

enum class HttpMethod {
  GET,
  POST,
  PUT,
  PATCH,
  DELETE,
}

data class HttpCommandConfig(
  val path: String,
  val method: String = HttpMethod.GET.name,
  val headersJson: String = "{}",
)

internal fun HttpCommandConfig.toJson(): String = flatJsonObjectOf(
  "path" to path,
  "method" to method,
  "headersJson" to headersJson,
)

internal fun String.toHttpCommandConfig(): HttpCommandConfig {
  val json = parseFlatJsonObject() ?: emptyMap()
  return HttpCommandConfig(
    path = json["path"] ?: json["url"].orEmpty(),
    method = json["method"].orEmpty().ifBlank { HttpMethod.GET.name },
    headersJson = json["headersJson"].orEmpty().ifBlank { "{}" },
  )
}

internal fun String.toCommandType(): CommandType =
  CommandType.entries.firstOrNull { it.name == uppercase() } ?: CommandType.SSH
