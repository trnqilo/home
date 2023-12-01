package trnqilo.telecomando.ui.executor

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import trnqilo.telecomando.execution.CommandExecutionState
import trnqilo.telecomando.execution.ExecutionStatus
import trnqilo.telecomando.execution.TargetExecutionState

class CommandExecutionPanelTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun explainsWhyRunIsDisabled() {
    composeRule.setContent {
      MaterialTheme {
        CommandExecutionPanel(
          command = "",
          hasTargets = false,
          state = CommandExecutionState(),
          onRun = {},
          onCancel = {},
        )
      }
    }

    composeRule.onNodeWithText("Run command").assertIsNotEnabled()
    composeRule.onNodeWithText("Enter a command to run.").assertExists()
  }

  @Test
  fun labelsOutputStreams() {
    composeRule.setContent {
      MaterialTheme {
        CommandExecutionPanel(
          command = "uptime",
          hasTargets = true,
          state = CommandExecutionState(
            targets = listOf(
              TargetExecutionState(
                targetId = 1,
                targetName = "Server",
                status = ExecutionStatus.Succeeded,
                stdout = "online",
                stderr = "warning",
                exitCode = 0,
              )
            )
          ),
          onRun = {},
          onCancel = {},
        )
      }
    }

    composeRule.onNodeWithText("Standard output").assertExists()
    composeRule.onNodeWithText("Standard error").assertExists()
    composeRule.onNodeWithText("online").assertExists()
    composeRule.onNodeWithText("warning").assertExists()
  }
}
