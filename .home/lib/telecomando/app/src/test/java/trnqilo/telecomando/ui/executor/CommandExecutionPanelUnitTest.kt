package trnqilo.telecomando.ui.executor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import trnqilo.telecomando.execution.ExecutionStatus
import trnqilo.telecomando.execution.TargetExecutionState

class CommandExecutionPanelUnitTest {
  @Test
  fun runRequiresCommandAndServer() {
    assertFalse(canRunCommand("", true))
    assertFalse(canRunCommand("uptime", false))
    assertTrue(canRunCommand("uptime", true))
  }

  @Test
  fun failedStatusIncludesNonzeroExitCode() {
    val state = TargetExecutionState(
      targetId = 1,
      targetName = "Server",
      status = ExecutionStatus.Failed,
      exitCode = 12,
    )

    assertEquals("Failed · exit 12", executionStatusText(state))
  }

  @Test
  fun guidanceExplainsWhyCommandCannotRun() {
    assertEquals("Enter a command to run.", commandExecutionGuidance("", false))
    assertEquals(
      "Select at least one server to run this command.",
      commandExecutionGuidance("uptime", false),
    )
    assertEquals("Run the command to see output.", commandExecutionGuidance("uptime", true))
  }
}
