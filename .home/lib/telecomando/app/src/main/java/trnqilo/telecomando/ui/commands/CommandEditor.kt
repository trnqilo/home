package trnqilo.telecomando.ui.commands

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import trnqilo.telecomando.command.CommandRunner
import trnqilo.telecomando.commands.CommandDraft
import trnqilo.telecomando.commands.CommandEditorStateHolder
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ServerEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.ConnectionType
import trnqilo.telecomando.execution.CommandExecutionRequest
import trnqilo.telecomando.execution.CommandExecutionStateHolder

@Composable
fun CommandEditor(
  onSave: (CommandWithServers, List<ServerEntity>) -> Unit,
  onBack: () -> Unit,
  allServers: List<ServerEntity>,
  model: CommandWithServers? = null,
) {
  val editorStateHolder = rememberSaveable(
    model?.command?.commandId,
    saver = commandEditorStateHolderSaver,
  ) {
    CommandEditorStateHolder(model.toCommandDraft())
  }
  val draft by editorStateHolder.draft.collectAsState()

  val coroutineScope = rememberCoroutineScope()
  val executionStateHolder = remember(coroutineScope) {
    CommandExecutionStateHolder(coroutineScope, CommandRunner())
  }
  val executionState by executionStateHolder.state.collectAsState()
  DisposableEffect(executionStateHolder) {
    onDispose(executionStateHolder::cancel)
  }

  val serverOptions = allServers.compatibleWith(draft.type).map(ServerEntity::toServerOption)
  val executionTargets = draft.toExecutionTargets(allServers)
  val previewCommand = draft.toCommandPreview(allServers, model?.command?.commandId ?: 0)

  CommandEditorContent(
    title = if (model == null) "Add command" else "Edit command",
    draft = draft,
    allServers = serverOptions,
    executionState = executionState,
    onNameChange = editorStateHolder::updateName,
    onCommandChange = editorStateHolder::updateCommand,
    onTypeChange = editorStateHolder::updateType,
    onHttpUrlChange = editorStateHolder::updateHttpPath,
    onHttpMethodChange = editorStateHolder::updateHttpMethod,
    onHttpHeadersChange = editorStateHolder::updateHttpHeadersJson,
    onAddServer = editorStateHolder::addServer,
    onRemoveServer = editorStateHolder::removeServer,
    onRun = {
      executionStateHolder.run(
        CommandExecutionRequest(
          command = previewCommand,
          targets = executionTargets,
        )
      )
    },
    onCancel = executionStateHolder::cancel,
    onSave = {
      val save = draft.toCommandSave(model, allServers)
      onSave(save.command, save.removedServers)
    },
    onBack = onBack,
  )
}

private val commandEditorStateHolderSaver = listSaver<CommandEditorStateHolder, Any>(
  save = { stateHolder ->
    val draft = stateHolder.draft.value
    listOf(
      draft.name,
      draft.command,
      ArrayList(draft.selectedServerIds),
      draft.type,
      draft.httpPath,
      draft.httpMethod,
      draft.httpHeadersJson,
    )
  },
  restore = { values ->
    @Suppress("UNCHECKED_CAST")
    CommandEditorStateHolder(
      CommandDraft(
        name = values[0] as String,
        command = values[1] as String,
        selectedServerIds = values[2] as List<Int>,
        type = values[3] as String,
        httpPath = values[4] as String,
        httpMethod = values[5] as String,
        httpHeadersJson = values[6] as String,
      )
    )
  },
)

private fun ServerEntity.toServerOption() = ServerOption(serverId, name)

internal fun List<ServerEntity>.compatibleWith(commandType: String): List<ServerEntity> {
  val compatibleType = if (commandType == CommandType.HTTP.name) {
    ConnectionType.HTTP.name
  } else {
    ConnectionType.SSH.name
  }
  return filter { it.type == compatibleType }
}
