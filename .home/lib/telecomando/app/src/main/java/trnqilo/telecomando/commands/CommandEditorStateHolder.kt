package trnqilo.telecomando.commands

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommandEditorStateHolder(initialDraft: CommandDraft = CommandDraft()) {
  private val mutableDraft = MutableStateFlow(initialDraft)
  val draft: StateFlow<CommandDraft> = mutableDraft.asStateFlow()

  fun updateName(name: String) {
    mutableDraft.update { it.copy(name = name) }
  }

  fun updateCommand(command: String) {
    mutableDraft.update { it.copy(command = command) }
  }

  fun updateType(type: String) {
    mutableDraft.update { it.copy(type = type, selectedServerIds = emptyList()) }
  }

  fun updateHttpPath(path: String) {
    mutableDraft.update { it.copy(httpPath = path) }
  }

  fun updateHttpMethod(method: String) {
    mutableDraft.update { it.copy(httpMethod = method) }
  }

  fun updateHttpHeadersJson(headersJson: String) {
    mutableDraft.update { it.copy(httpHeadersJson = headersJson) }
  }

  fun addServer(serverId: Int) {
    mutableDraft.update { draft ->
      if (serverId in draft.selectedServerIds) {
        draft
      } else {
        draft.copy(selectedServerIds = draft.selectedServerIds + serverId)
      }
    }
  }

  fun removeServer(serverId: Int) {
    mutableDraft.update { draft ->
      draft.copy(selectedServerIds = draft.selectedServerIds - serverId)
    }
  }
}
