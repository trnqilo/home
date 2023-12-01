package trnqilo.telecomando.command

import org.junit.Assert.assertEquals
import org.junit.Test
import trnqilo.telecomando.execution.CommandExecutionContext
import trnqilo.telecomando.execution.CommandOutputSummary

class CommandTemplateRendererTest {
  @Test
  fun rendersResultAliasFromUpstreamSummary() {
    val context = CommandExecutionContext(
      upstream = CommandOutputSummary(
        commandName = "Previous",
        commandType = "HTTP",
        succeeded = true,
        result = "primary",
        stdout = "stdout",
        stderr = "stderr",
        exitCode = 0,
        httpStatus = 201,
        responseBody = "body",
      )
    )

    val rendered = "{{result}}|{{result.stdout}}|{{result.stderr}}|{{result.exitCode}}|{{result.httpStatus}}|{{result.responseBody}}"
      .renderCommandTemplate(context)

    assertEquals("primary|stdout|stderr|0|201|body", rendered)
  }
}
