package trnqilo.telecomando.command

import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.HttpCommandConfig
import trnqilo.telecomando.data.HttpMethod
import trnqilo.telecomando.data.parseFlatJsonObject
import trnqilo.telecomando.data.toHttpCommandConfig
import trnqilo.telecomando.execution.CommandExecutionContext
import trnqilo.telecomando.execution.HttpTarget

internal class HttpCommandEventStream {
  fun execute(command: CommandWithServers, target: HttpTarget, context: CommandExecutionContext): Flow<CommandEvent> = flow {
    val config = command.command.configJson.takeIf { it.isNotBlank() }?.toHttpCommandConfig()
      ?: HttpCommandConfig(path = "")
    val resolvedUrl = resolveHttpUrl(
      baseUrl = target.baseUrl.renderCommandTemplate(context),
      path = config.path.renderCommandTemplate(context),
    )
    if (resolvedUrl.isBlank()) {
      emit(CommandEvent.Failed("HTTP command is missing a connection URL."))
      return@flow
    }

    var connection: HttpURLConnection? = null
    try {
      connection = URL(resolvedUrl).openConnection() as HttpURLConnection
      connection.requestMethod = config.method.ifBlank { HttpMethod.GET.name }
      connection.connectTimeout = 10_000
      connection.readTimeout = 10_000
      connection.doInput = true
      val defaultHeaders = target.defaultHeadersJson.renderCommandTemplate(context).parseFlatJsonObject()
        ?: error("HTTP connection has malformed default headers JSON.")
      val requestHeaders = config.headersJson.renderCommandTemplate(context).parseFlatJsonObject()
        ?: error("HTTP command has malformed headers JSON.")
      (defaultHeaders + requestHeaders).forEach { (key, value) ->
        connection.setRequestProperty(key, value)
      }
      val body = command.command.command.renderCommandTemplate(context)
      if (connection.requestMethod != HttpMethod.GET.name && connection.requestMethod != HttpMethod.DELETE.name) {
        connection.doOutput = true
        connection.outputStream.use { output ->
          BufferedWriter(OutputStreamWriter(output, Charsets.UTF_8)).use { writer ->
            writer.write(body)
          }
        }
      }
      val statusCode = connection.responseCode
      val stream = runCatching { connection.inputStream }.getOrNull() ?: connection.errorStream
      val responseBody = stream?.use { input ->
        InputStreamReader(input).use { reader -> reader.readText() }
      }.orEmpty()
      if (responseBody.isNotBlank()) {
        emit(CommandEvent.Stdout(responseBody))
      }
      emit(CommandEvent.Completed(if (statusCode in 200..299) 0 else statusCode))
    } catch (exception: Exception) {
      val detail = exception.message?.takeIf(String::isNotBlank)
      emit(CommandEvent.Failed(detail ?: "${exception.javaClass.simpleName} occurred."))
    } finally {
      connection?.disconnect()
    }
  }.flowOn(Dispatchers.IO)
}

internal fun resolveHttpUrl(baseUrl: String, path: String): String = when {
  path.startsWith("http://") || path.startsWith("https://") -> path
  baseUrl.isBlank() -> path
  path.isBlank() -> baseUrl
  else -> "${baseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
