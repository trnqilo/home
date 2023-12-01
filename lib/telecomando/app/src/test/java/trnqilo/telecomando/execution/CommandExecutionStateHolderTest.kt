package trnqilo.telecomando.execution

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import trnqilo.telecomando.command.CommandEvent
import trnqilo.telecomando.command.CommandExecutionEngine
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers

@OptIn(ExperimentalCoroutinesApi::class)
class CommandExecutionStateHolderTest {
  @Test
  fun executesTargetsSequentiallyAndContinuesAfterFailure() = runTest {
    val executionOrder = mutableListOf<String>()
    val ports = mutableListOf<Int>()
    val first = target(1)
    val second = target(2)
    val stream = CommandExecutionEngine { request ->
      val target = request.targets.single() as SshTarget
      executionOrder += target.address
      ports += target.port
      if (target.address == first.address) {
        flowOf(CommandEvent.Failed("unreachable"))
      } else {
        flowOf(CommandEvent.Stdout("done"), CommandEvent.Completed(0))
      }
    }
    val stateHolder = CommandExecutionStateHolder(this, stream)

    stateHolder.run(request("uptime", listOf(first, second)))
    advanceUntilIdle()

    assertEquals(listOf(first.address, second.address), executionOrder)
    assertEquals(listOf(first.port, second.port), ports)
    assertEquals(ExecutionStatus.Failed, stateHolder.state.value.targets[0].status)
    assertEquals("unreachable", stateHolder.state.value.targets[0].error)
    assertEquals(ExecutionStatus.Succeeded, stateHolder.state.value.targets[1].status)
    assertEquals("done", stateHolder.state.value.targets[1].stdout)
    assertFalse(stateHolder.state.value.isRunning)
  }

  @Test
  fun preservesStdoutStderrAndNonzeroExitStatus() = runTest {
    val stream = CommandExecutionEngine {
      flowOf(
        CommandEvent.Stdout("output"),
        CommandEvent.Stderr("problem"),
        CommandEvent.Completed(4),
      )
    }
    val stateHolder = CommandExecutionStateHolder(this, stream)

    stateHolder.run(request("false", listOf(target(1))))
    advanceUntilIdle()

    val result = stateHolder.state.value.targets.single()
    assertEquals("output", result.stdout)
    assertEquals("problem", result.stderr)
    assertEquals(4, result.exitCode)
    assertEquals(ExecutionStatus.Failed, result.status)
  }

  @Test
  fun cancelMarksRunningTargetAndLeavesPendingTargetsUntouched() = runTest {
    val neverCompletes: Flow<CommandEvent> = flow { awaitCancellation() }
    val stateHolder = CommandExecutionStateHolder(this, CommandExecutionEngine { neverCompletes })

    stateHolder.run(request("tail -f file", listOf(target(1), target(2))))
    advanceUntilIdle()
    stateHolder.cancel()
    advanceUntilIdle()

    assertEquals(ExecutionStatus.Cancelled, stateHolder.state.value.targets[0].status)
    assertEquals(ExecutionStatus.Pending, stateHolder.state.value.targets[1].status)
    assertFalse(stateHolder.state.value.isRunning)
  }

  @Test
  fun rerunReplacesPreviousResults() = runTest {
    var events = flowOf(CommandEvent.Stdout("old"), CommandEvent.Completed(0))
    val stateHolder = CommandExecutionStateHolder(this, CommandExecutionEngine { events })
    val target = target(1)

    stateHolder.run(request("first", listOf(target)))
    advanceUntilIdle()
    events = flowOf(CommandEvent.Stdout("new"), CommandEvent.Completed(0))
    stateHolder.run(request("second", listOf(target)))
    advanceUntilIdle()

    assertEquals("new", stateHolder.state.value.targets.single().stdout)
  }

  private fun target(id: Int) = SshTarget(
    id = id,
    name = "Server $id",
    address = "server$id.test",
    port = 2200 + id,
    username = "user",
    password = "password",
  )

  private fun request(commandText: String, targets: List<CommandTarget>) = CommandExecutionRequest(
    command = CommandWithServers(
      CommandEntity(
        name = "Test",
        command = commandText,
        serverIds = "",
        type = CommandType.SSH.name,
        configJson = "{}",
      ),
      emptyList(),
    ),
    targets = targets,
  )
}
