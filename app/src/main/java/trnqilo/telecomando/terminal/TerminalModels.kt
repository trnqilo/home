package trnqilo.telecomando.terminal

import androidx.compose.ui.text.AnnotatedString
import trnqilo.telecomando.ssh.SshConnectionRequest

data class TerminalConnectionRequest(
  val title: String,
  val connection: SshConnectionRequest,
)

sealed interface TerminalEvent {
  data class Output(val text: String) : TerminalEvent
  data class Connected(val message: String? = null) : TerminalEvent
  data class Disconnected(val exitCode: Int? = null) : TerminalEvent
  data class Failed(val message: String) : TerminalEvent
}

sealed interface TerminalStatus {
  data object Idle : TerminalStatus
  data object Connecting : TerminalStatus
  data object Connected : TerminalStatus
  data object Disconnected : TerminalStatus
  data class Failed(val message: String) : TerminalStatus
}

data class TerminalState(
  val title: String = "",
  val status: TerminalStatus = TerminalStatus.Idle,
  val output: String = "",
  val renderedOutput: AnnotatedString = AnnotatedString(""),
  val cursor: TerminalCursor? = null,
)

data class TerminalCursor(
  val offset: Int,
  val visible: Boolean,
)

interface TerminalSession : AutoCloseable {
  val events: kotlinx.coroutines.flow.Flow<TerminalEvent>

  suspend fun sendInput(text: String)

  fun resize(columns: Int, rows: Int)
}

fun interface TerminalSessionFactory {
  fun open(request: TerminalConnectionRequest): TerminalSession
}
