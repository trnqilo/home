package trnqilo.telecomando.command

import java.net.InetAddress
import java.net.ServerSocket
import java.util.Collections
import kotlin.concurrent.thread
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.HttpCommandConfig
import trnqilo.telecomando.data.toJson
import trnqilo.telecomando.execution.CommandExecutionRequest
import trnqilo.telecomando.execution.CommandExecutionStateHolder
import trnqilo.telecomando.execution.ExecutionStatus
import trnqilo.telecomando.execution.HttpTarget

class HttpMultiConnectionExecutionTest {
  private lateinit var server: ServerSocket
  private lateinit var serverThread: Thread
  private val receivedHeaders = Collections.synchronizedList(mutableListOf<String?>())

  @Before
  fun startServer() {
    server = ServerSocket(0, 2, InetAddress.getByName("127.0.0.1"))
    serverThread = thread(name = "http-command-test-server") {
      repeat(2) {
        server.accept().use { socket ->
          val reader = socket.getInputStream().bufferedReader()
          val requestLine = reader.readLine()
          var environment: String? = null
          while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
            if (line.startsWith("X-Environment:", ignoreCase = true)) {
              environment = line.substringAfter(':').trim()
            }
          }
          receivedHeaders += environment
          val response = if (requestLine.contains("/one/")) "one" else "two"
          socket.getOutputStream().bufferedWriter().use { writer ->
            writer.write("HTTP/1.1 200 OK\r\n")
            writer.write("Content-Length: ${response.length}\r\n")
            writer.write("Connection: close\r\n\r\n")
            writer.write(response)
          }
        }
      }
    }
  }

  @After
  fun stopServer() {
    server.close()
    serverThread.join(1_000)
  }

  @Test
  fun executesOncePerHttpConnectionAndKeepsResultsSeparate() = runBlocking {
    val command = CommandWithServers(
      command = CommandEntity(
        name = "Health",
        command = "",
        serverIds = "1,2",
        type = CommandType.HTTP.name,
        configJson = HttpCommandConfig(
          path = "/health",
          headersJson = "{\"X-Environment\":\"command\"}",
        ).toJson(),
      ),
      servers = emptyList(),
    )
    val base = "http://127.0.0.1:${server.localPort}"
    val targets = listOf(
      HttpTarget(1, "One", "$base/one", "{\"X-Environment\":\"default\"}"),
      HttpTarget(2, "Two", "$base/two", "{\"X-Environment\":\"default\"}"),
    )
    val stateHolder = CommandExecutionStateHolder(this, CommandRunner())

    stateHolder.run(CommandExecutionRequest(command, targets))
    withTimeout(5_000) {
      stateHolder.state.first { !it.isRunning }
    }

    val results = stateHolder.state.value.targets
    assertEquals(listOf(ExecutionStatus.Succeeded, ExecutionStatus.Succeeded), results.map { it.status })
    assertEquals(listOf("one", "two"), results.map { it.stdout })
    assertEquals(listOf("command", "command"), receivedHeaders)
    assertTrue(!stateHolder.state.value.isRunning)
  }
}
