package trnqilo.telecomando.ui.terminal

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.terminal.TerminalConnectionRequest
import trnqilo.telecomando.terminal.TerminalEvent
import trnqilo.telecomando.terminal.TerminalSession
import trnqilo.telecomando.terminal.TerminalSessionFactory

class TerminalScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun rendersOutputAndSendsInput() {
    val session = FakeTerminalSession()
    composeRule.setContent {
      MaterialTheme {
        TerminalScreen(
          server = server(),
          onBack = {},
          sessionFactory = TerminalSessionFactory { session },
        )
      }
    }

    session.emit(TerminalEvent.Connected())
    session.emit(TerminalEvent.Output("hello\n"))

    composeRule.onNodeWithText("hello").assertExists()
    composeRule.onNodeWithText("Esc").assertExists()
    composeRule.onNodeWithText("Esc").performClick()

    assertEquals(listOf("\u001B"), session.inputs)
  }

  @Test
  fun altIsConsumedByTheNextButtonPress() {
    val session = FakeTerminalSession()
    composeRule.setContent {
      MaterialTheme {
        TerminalScreen(
          server = server(),
          onBack = {},
          sessionFactory = TerminalSessionFactory { session },
        )
      }
    }

    session.emit(TerminalEvent.Connected())

    composeRule.onNodeWithText("Alt").assertExists().performClick()
    composeRule.onNodeWithText("Esc").performClick()

    assertEquals(listOf("\u001B\u001B"), session.inputs)
  }

  @Test
  fun ctrlIsAppliedToSoftwareKeyboardInput() {
    val session = FakeTerminalSession()
    composeRule.setContent {
      MaterialTheme {
        TerminalScreen(
          server = server(),
          onBack = {},
          sessionFactory = TerminalSessionFactory { session },
        )
      }
    }

    session.emit(TerminalEvent.Connected())

    composeRule.onNodeWithText("Ctrl").performClick()
    composeRule.onNodeWithTag("terminalInput").performTextInput("c")

    composeRule.waitUntil { session.inputs.contains("\u0003") }
    assertEquals(listOf("\u0003"), session.inputs)
  }

  private fun server() = ServerEntity(
    serverId = 1,
    name = "Local",
    address = "127.0.0.1",
    port = 22,
    username = "user",
    password = "password",
    key = "",
  )

  private class FakeTerminalSession : TerminalSession {
    private val _events = MutableSharedFlow<TerminalEvent>(replay = 1, extraBufferCapacity = 8)
    val inputs = CopyOnWriteArrayList<String>()

    override val events = _events.asSharedFlow()

    fun emit(event: TerminalEvent) {
      _events.tryEmit(event)
    }

    override suspend fun sendInput(text: String) {
      inputs += text
    }

    override fun resize(columns: Int, rows: Int) = Unit

    override fun close() = Unit
  }
}
