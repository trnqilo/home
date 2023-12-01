package trnqilo.telecomando.ui.servers

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.data.ConnectionType
import org.junit.Assert.assertEquals

class ServerEditorTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun invalidPortPreventsSaving() {
    composeRule.setContent {
      MaterialTheme {
        ServerEditor(
          onSave = {},
          onBack = {},
          model = ServerEntity(
            serverId = 1,
            name = "Local",
            address = "127.0.0.1",
            port = 22,
            username = "user",
            password = "",
            key = "",
          ),
        )
      }
    }

    composeRule.onNodeWithTag("server-port").performTextClearance()
    composeRule.onNodeWithTag("server-port").performTextInput("70000")

    composeRule.onNodeWithText("Enter a port from 1 to 65535").assertExists()
    composeRule.onNodeWithContentDescription("Save").assertIsNotEnabled()
  }

  @Test
  fun exposesConnectActionForExistingServers() {
    var connectedServer: ServerEntity? = null
    composeRule.setContent {
      MaterialTheme {
        ServerEditor(
          onSave = {},
          onConnect = { connectedServer = it },
          onBack = {},
          model = ServerEntity(
            serverId = 7,
            name = "Local",
            address = "127.0.0.1",
            port = 22,
            username = "user",
            password = "secret",
            key = "",
          ),
        )
      }
    }

    composeRule.onNodeWithText("Connect").performClick()

    assertEquals(7, connectedServer?.serverId)
  }

  @Test
  fun editsHttpConnectionWithoutTerminalAction() {
    var savedConnection: ServerEntity? = null
    composeRule.setContent {
      MaterialTheme {
        ServerEditor(
          onSave = { savedConnection = it },
          onBack = {},
          model = ServerEntity.http(
            name = "API",
            baseUrl = "https://api.example.test",
            connectionId = 8,
          ),
        )
      }
    }

    composeRule.onNodeWithText("Base URL").assertExists()
    composeRule.onNodeWithText("Connect").assertDoesNotExist()
    composeRule.onNodeWithContentDescription("Save").performClick()

    assertEquals(ConnectionType.HTTP.name, savedConnection?.type)
    assertEquals(8, savedConnection?.connectionId)
  }
}
