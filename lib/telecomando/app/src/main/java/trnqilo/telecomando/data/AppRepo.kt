package trnqilo.telecomando.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import trnqilo.telecomando.automation.AutomationSave
import trnqilo.telecomando.automation.toEntity

class AppRepo(private val dao: CommandServerDao) {
  suspend fun addServer(server: ServerEntity) {
    withContext(IO) {
      dao.insertServer(server)
    }
  }

  suspend fun addCommand(command: CommandWithServers) {
    withContext(IO) {
      val commandId = dao.insertCommand(command.command).toInt()
      command.servers.forEach { server ->
        dao.insertCommandServerCrossRef(CommandServerCrossRef(commandId, server.serverId))
      }
    }
  }

  suspend fun removeServerAssociations(commandId: Int, servers: List<ServerEntity>) {
    withContext(IO) {
      servers.forEach { server ->
        dao.deleteCommandServerCrossRef(CommandServerCrossRef(commandId, server.serverId))
      }
    }
  }

  fun getServers(): Flow<List<ServerEntity>> = dao.getServers()

  fun getCommands(): Flow<List<CommandWithServers>> = dao.getCommandsWithServers()

  fun getAutomations(): Flow<List<AutomationRuleWithSteps>> = dao.getAutomationRulesWithSteps()

  fun getAutomationRules(): Flow<List<AutomationRuleEntity>> = dao.getAutomationRules()

  suspend fun getAutomation(ruleId: Int): AutomationRuleWithSteps? = withContext(IO) {
    dao.getAutomationRule(ruleId)
  }

  suspend fun getCommandWithServers(commandId: Int): CommandWithServers? = withContext(IO) {
    dao.getCommandWithServers(commandId)
  }

  suspend fun saveAutomation(save: AutomationSave): Int = withContext(IO) {
    val ruleId = dao.insertAutomationRule(save.rule).toInt()
    dao.deleteAutomationInitiators(ruleId)
    dao.deleteAutomationSteps(ruleId)
    save.initiators.forEachIndexed { index, initiator ->
      dao.insertAutomationInitiator(
        initiator.toEntity(ruleId = ruleId, orderIndex = index)
      ).toInt()
    }
    val savedStepIds = save.steps.mapIndexed { index, step ->
      dao.insertAutomationStep(
        AutomationStepEntity(
          ruleId = ruleId,
          orderIndex = index,
          commandId = requireNotNull(step.commandId),
        )
      ).toInt()
    }
    save.steps.zip(savedStepIds).forEach { (step, savedStepId) ->
      dao.updateAutomationStepLinks(
        stepId = savedStepId,
        successNextStepId = step.successNextKey?.let { nextKey -> stepIdByKey(nextKey, save.steps, savedStepIds) },
        failureNextStepId = step.failureNextKey?.let { nextKey -> stepIdByKey(nextKey, save.steps, savedStepIds) },
      )
    }
    ruleId
  }

  suspend fun deleteAutomation(ruleId: Int) {
    withContext(IO) {
      dao.deleteAutomation(ruleId)
    }
  }
}

private fun stepIdByKey(
  key: Long,
  steps: List<trnqilo.telecomando.automation.AutomationStepDraft>,
  savedStepIds: List<Int>,
): Int? {
  val index = steps.indexOfFirst { it.key == key }
  return savedStepIds.getOrNull(index)
}
