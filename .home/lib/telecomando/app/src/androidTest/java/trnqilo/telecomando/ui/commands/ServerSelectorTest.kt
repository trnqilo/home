package trnqilo.telecomando.ui.commands

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ServerSelectorTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun exposesLabeledAddAndRemoveControls() {
    composeRule.setContent {
      MaterialTheme {
        ServerSelector(
          allServers = listOf(ServerOption(1, "Server"), ServerOption(2, "Other")),
          selectedServerIds = listOf(1),
          onAddServer = {},
          onRemoveServer = {},
        )
      }
    }

    composeRule.onNodeWithText("Add connection").assertExists()
    composeRule.onNodeWithContentDescription("Remove Server").assertExists()
  }

  @Test
  fun explainsEmptySelection() {
    composeRule.setContent {
      MaterialTheme {
        ServerSelector(
          allServers = emptyList(),
          selectedServerIds = emptyList(),
          onAddServer = {},
          onRemoveServer = {},
        )
      }
    }

    composeRule.onNodeWithText("No connections selected").assertExists()
  }
}
