package trnqilo.telecomando.command

import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandRunnerTest {
  @Test
  fun emitsOutputAndExitStatus() = runTest {
    val connection = FakeConnection(stdoutText = "hello\n", stderrText = "warning\n", exitStatus = 7)
    val runner = JschCommandEventStream(CommandConnectionFactory { connection })

    val events = runner.execute(request("echo hello")).toList()

    assertEquals(
      listOf(
        CommandEvent.Stdout("hello\n"),
        CommandEvent.Stderr("warning\n"),
        CommandEvent.Completed(7),
      ),
      events,
    )
    assertTrue(connection.closed)
  }

  @Test
  fun emitsFailureWhenConnectionCannotBeOpened() = runTest {
    val runner = JschCommandEventStream(CommandConnectionFactory {
      throw IllegalStateException("connection failed")
    })

    assertEquals(
      listOf(CommandEvent.Failed("connection failed")),
      runner.execute(request("pwd")).toList(),
    )
  }

  private fun request(command: String) = SshCommandRequest(
    command = command,
    destination = "example.test",
  )

  private class FakeConnection(
    stdoutText: String = "",
    stderrText: String = "",
    override val exitStatus: Int = 0,
  ) : CommandConnection {
    override val stdout: InputStream = ByteArrayInputStream(stdoutText.toByteArray())
    override val stderr: InputStream = ByteArrayInputStream(stderrText.toByteArray())
    override val isClosed = true
    var closed = false

    override fun close() {
      closed = true
    }
  }
}
