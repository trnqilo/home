package trnqilo.telecomando.automation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Test
import trnqilo.telecomando.command.CommandEvent
import trnqilo.telecomando.command.CommandExecutionEngine
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ConnectionEntity
import trnqilo.telecomando.execution.CommandExecutionContext
import trnqilo.telecomando.execution.CommandExecutionRequest

class AutomationChainRunnerTest {
  @Test
  fun executesSuccessBranchAndPassesPreviousResult() = kotlinx.coroutines.test.runTest {
    val engine = FakeEngine(
      responses = mapOf(
        "First" to listOf(CommandEvent.Stdout("alpha"), CommandEvent.Completed(0)),
        "Success" to listOf(CommandEvent.Stdout("bravo"), CommandEvent.Completed(0)),
      )
    )
    val runner = AutomationChainRunner(AutomationCommandRunner(engine))
    val rule = rule(
      steps = listOf(
        step(stepId = 1, orderIndex = 0, commandId = 1, successNextStepId = 2, failureNextStepId = 3),
        step(stepId = 2, orderIndex = 1, commandId = 2),
        step(stepId = 3, orderIndex = 2, commandId = 3),
      )
    )

    val result = runner.execute(rule) { commandId -> commands[commandId] }

    assertEquals(listOf(1, 2), result.steps.map { it.command.commandId })
    assertEquals(listOf(null, "alpha"), engine.contexts.map { it.upstream?.result })
    assertEquals("alpha", result.steps.first().command.summary.result)
    assertEquals(2, result.steps.first().nextStepId)
  }

  @Test
  fun executesFailureBranchAndPassesPreviousResult() = kotlinx.coroutines.test.runTest {
    val engine = FakeEngine(
      responses = mapOf(
        "First" to listOf(CommandEvent.Stdout("omega"), CommandEvent.Completed(1)),
        "Failure" to listOf(CommandEvent.Stdout("charlie"), CommandEvent.Completed(0)),
      )
    )
    val runner = AutomationChainRunner(AutomationCommandRunner(engine))
    val rule = rule(
      steps = listOf(
        step(stepId = 1, orderIndex = 0, commandId = 1, successNextStepId = 2, failureNextStepId = 3),
        step(stepId = 2, orderIndex = 1, commandId = 2),
        step(stepId = 3, orderIndex = 2, commandId = 3),
      )
    )

    val result = runner.execute(rule) { commandId -> commands[commandId] }

    assertEquals(listOf(1, 3), result.steps.map { it.command.commandId })
    assertEquals(listOf(null, "omega"), engine.contexts.map { it.upstream?.result })
    assertEquals(3, result.steps.first().nextStepId)
  }

  private val commands = mapOf(
    1 to command(1, "First"),
    2 to command(2, "Success"),
    3 to command(3, "Failure"),
  )

  private fun rule(steps: List<trnqilo.telecomando.data.AutomationStepEntity>) =
    trnqilo.telecomando.data.AutomationRuleWithSteps(
      rule = trnqilo.telecomando.data.AutomationRuleEntity(
        name = "Chain",
        enabled = true,
        initiatorType = AutomationInitiatorType.Alarm.name,
        initiatorDelayMinutes = 5,
        commandId = 1,
        completionPolicy = "Chain",
        ruleId = 42,
      ),
      initiators = listOf(
        trnqilo.telecomando.data.AutomationInitiatorEntity(
          ruleId = 42,
          orderIndex = 0,
          type = AutomationInitiatorType.Alarm.name,
          configJson = """{"delayMinutes":"5"}""",
          initiatorId = 1,
        ),
      ),
      steps = steps,
    )

  private fun step(
    stepId: Int,
    orderIndex: Int,
    commandId: Int,
    successNextStepId: Int? = null,
    failureNextStepId: Int? = null,
  ) = trnqilo.telecomando.data.AutomationStepEntity(
    ruleId = 42,
    orderIndex = orderIndex,
    commandId = commandId,
    successNextStepId = successNextStepId,
    failureNextStepId = failureNextStepId,
    stepId = stepId,
  )

  private fun command(id: Int, name: String): CommandWithServers =
    CommandWithServers(
      command = CommandEntity(
        name = name,
        command = "body-$name",
        serverIds = id.toString(),
        type = CommandType.HTTP.name,
        configJson = """{"url":"https://$name.test"}""",
        commandId = id,
      ),
      servers = listOf(
        ConnectionEntity.http(
          name = "$name endpoint",
          baseUrl = "https://$name.test",
          connectionId = id,
        )
      ),
    )

  private class FakeEngine(
    private val responses: Map<String, List<CommandEvent>>,
  ) : CommandExecutionEngine {
    val contexts = mutableListOf<CommandExecutionContext>()

    override fun execute(request: CommandExecutionRequest): Flow<CommandEvent> = flow {
      contexts += request.context
      responses[request.command.command.name].orEmpty().forEach { emit(it) }
    }
  }
}
