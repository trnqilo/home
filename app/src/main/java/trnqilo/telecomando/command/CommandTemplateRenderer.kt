package trnqilo.telecomando.command

import trnqilo.telecomando.data.escapeJsonString
import trnqilo.telecomando.execution.CommandExecutionContext

internal fun String.renderCommandTemplate(context: CommandExecutionContext): String {
  val upstream = context.upstream
  if (upstream == null) return this
  return replace("{{upstream.commandName}}", upstream.commandName)
    .replace("{{upstream.commandType}}", upstream.commandType)
    .replace("{{upstream.status}}", if (upstream.succeeded) "success" else "failure")
    .replace("{{upstream.succeeded}}", upstream.succeeded.toString())
    .replace("{{upstream.exitCode}}", upstream.exitCode?.toString().orEmpty())
    .replace("{{upstream.httpStatus}}", upstream.httpStatus?.toString().orEmpty())
    .replace("{{result}}", upstream.primaryOutput)
    .replace("{{upstream.stdout}}", upstream.stdout)
    .replace("{{upstream.stderr}}", upstream.stderr)
    .replace("{{upstream.responseBody}}", upstream.responseBody.orEmpty())
    .replace("{{result.stdout}}", upstream.stdout)
    .replace("{{result.stderr}}", upstream.stderr)
    .replace("{{result.exitCode}}", upstream.exitCode?.toString().orEmpty())
    .replace("{{result.httpStatus}}", upstream.httpStatus?.toString().orEmpty())
    .replace("{{result.responseBody}}", upstream.responseBody.orEmpty())
    .replace("{{upstream.stdoutJson}}", "\"${upstream.stdout.escapeJsonString()}\"")
    .replace("{{upstream.stderrJson}}", "\"${upstream.stderr.escapeJsonString()}\"")
    .replace("{{upstream.responseBodyJson}}", "\"${upstream.responseBody.orEmpty().escapeJsonString()}\"")
}
