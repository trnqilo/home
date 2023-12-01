package trnqilo.telecomando.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import trnqilo.telecomando.automation.AutomationSave
import trnqilo.telecomando.automation.AutomationScheduler
import trnqilo.telecomando.data.AppRepo
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ServerEntity

class AppViewModel(
  private val repo: AppRepo,
  private val automationScheduler: AutomationScheduler,
) : ViewModel() {
  val commands = repo.getCommands()
  val connections = repo.getServers()
  val automations = repo.getAutomations()

  fun addCommand(commandWithServers: CommandWithServers, removedServers: List<ServerEntity>) {
    viewModelScope.launch(IO) {
      commandWithServers.apply {
        repo.addCommand(this)
        if (command.commandId != 0 && removedServers.isNotEmpty()) {
          repo.removeServerAssociations(command.commandId, removedServers)
        }
      }
    }
  }

  fun addConnection(serverEntity: ServerEntity) {
    viewModelScope.launch(IO) {
      repo.addServer(serverEntity)
    }
  }

  val servers get() = connections
  fun addServer(serverEntity: ServerEntity) = addConnection(serverEntity)

  fun addAutomation(save: AutomationSave) {
    viewModelScope.launch(IO) {
      val ruleId = repo.saveAutomation(save)
      automationScheduler.sync(ruleId)
    }
  }

  fun deleteAutomation(ruleId: Int) {
    viewModelScope.launch(IO) {
      repo.deleteAutomation(ruleId)
      automationScheduler.cancel(ruleId)
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(
    private val repo: AppRepo,
    private val automationScheduler: AutomationScheduler,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      AppViewModel(repo, automationScheduler) as T
  }
}
