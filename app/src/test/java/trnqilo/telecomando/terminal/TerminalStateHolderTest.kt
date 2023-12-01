package trnqilo.telecomando.terminal

import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import trnqilo.telecomando.ssh.SshConnectionRequest

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalStateHolderTest {
  @Test
  fun connectsStreamsInputAndDisconnects() = runTest {
    val session = FakeTerminalSession()
    val stateHolder = TerminalStateHolder(
      this,
      TerminalSessionFactory { session },
      StandardTestDispatcher(testScheduler),
    )

    stateHolder.connect(request())
    advanceUntilIdle()
    assertEquals(TerminalStatus.Connecting, stateHolder.state.value.status)

    session.emit(TerminalEvent.Connected())
    session.emit(TerminalEvent.Output("hello"))
    advanceUntilIdle()

    assertEquals(TerminalStatus.Connected, stateHolder.state.value.status)
    assertEquals("hello", stateHolder.state.value.output)

    stateHolder.sendInput("ls\n")
    advanceUntilIdle()
    assertEquals(listOf("ls\n"), session.inputs)

    stateHolder.disconnect()
    advanceUntilIdle()
    assertEquals(TerminalStatus.Disconnected, stateHolder.state.value.status)
    assertTrue(session.closed)
  }

  @Test
  fun reportsFailuresFromFactory() = runTest {
    val stateHolder = TerminalStateHolder(
      this,
      TerminalSessionFactory { throw IllegalStateException("connection failed") },
      StandardTestDispatcher(testScheduler),
    )

    stateHolder.connect(request())
    advanceUntilIdle()

    val status = stateHolder.state.value.status
    assertTrue(status is TerminalStatus.Failed)
    assertEquals("connection failed", (status as TerminalStatus.Failed).message)
  }

  private fun request() = TerminalConnectionRequest(
    title = "Local",
    connection = SshConnectionRequest(
      destination = "example.test",
      port = 22,
      user = "user",
      password = "password",
    ),
  )

  private class FakeTerminalSession : TerminalSession {
    private val _events = MutableSharedFlow<TerminalEvent>(replay = 1, extraBufferCapacity = 8)
    val inputs = CopyOnWriteArrayList<String>()
    var closed = false

    override val events = _events.asSharedFlow()

    fun emit(event: TerminalEvent) {
      _events.tryEmit(event)
    }

    override suspend fun sendInput(text: String) {
      inputs += text
    }

    override fun resize(columns: Int, rows: Int) = Unit

    override fun close() {
      closed = true
    }
  }
}
