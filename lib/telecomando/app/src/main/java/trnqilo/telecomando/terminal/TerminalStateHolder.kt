package trnqilo.telecomando.terminal

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TerminalStateHolder(
  private val scope: CoroutineScope,
  private val sessionFactory: TerminalSessionFactory,
  private val dispatcher: CoroutineDispatcher = IO,
) {
  private val mutableState = MutableStateFlow(TerminalState())
  val state: StateFlow<TerminalState> = mutableState.asStateFlow()
  private var renderer = TerminalRenderer()

  private var session: TerminalSession? = null
  private var connectJob: Job? = null

  fun connect(request: TerminalConnectionRequest) {
    disconnect()
    renderer = TerminalRenderer()
    logDebug("Connecting terminal request for ${request.connection.destination}:${request.connection.port}")
    mutableState.value = TerminalState(
      title = request.title,
      status = TerminalStatus.Connecting,
    )
    connectJob = scope.launch {
      try {
        val activeSession = withContext(dispatcher) { sessionFactory.open(request) }
        session = activeSession
        logDebug("Terminal session opened")
        activeSession.events.collect { event ->
          mutableState.update { state -> state.reduce(request.title, event) }
        }
      } catch (exception: CancellationException) {
        throw exception
      } catch (exception: Exception) {
        logError("Terminal connect failed", exception)
        mutableState.update {
          it.copy(status = TerminalStatus.Failed(exception.message ?: "Connection failed."))
        }
      } finally {
        session?.close()
        session = null
      }
    }
  }

  fun sendInput(text: String) {
    val activeSession = session ?: return
    logDebug("Sending terminal input length=${text.length}")
    scope.launch {
      withContext(dispatcher) {
        runCatching {
          activeSession.sendInput(text)
        }.onFailure { throwable ->
          logError("Terminal send input failed", throwable)
        }
      }
    }
  }

  fun resize(columns: Int, rows: Int) {
    session?.resize(columns, rows)
    mutableState.update { state ->
      state.copy(
        renderedOutput = renderer.resize(columns, rows),
        cursor = renderer.cursor(),
      )
    }
  }

  fun disconnect() {
    connectJob?.cancel()
    connectJob = null
    session?.close()
    session = null
    logDebug("Terminal session disconnected")
    mutableState.update {
      if (it.status == TerminalStatus.Connecting || it.status == TerminalStatus.Connected) {
        it.copy(status = TerminalStatus.Disconnected)
      } else {
        it
      }
    }
  }

  private fun TerminalState.reduce(title: String, event: TerminalEvent): TerminalState = when (event) {
    is TerminalEvent.Output -> copy(
      title = title,
      status = TerminalStatus.Connected,
      output = output + event.text,
      renderedOutput = renderer.append(event.text),
      cursor = renderer.cursor(),
    )
    is TerminalEvent.Connected -> copy(
      title = title,
      status = TerminalStatus.Connected,
      renderedOutput = renderer.render(),
      cursor = renderer.cursor(),
    )
    is TerminalEvent.Disconnected -> copy(
      title = title,
      status = TerminalStatus.Disconnected,
    )
    is TerminalEvent.Failed -> copy(
      title = title,
      status = TerminalStatus.Failed(event.message),
    )
  }

  private companion object {
    const val TAG = "TerminalSSH"
  }
}

private fun logDebug(message: String) {
  runCatching { Log.d("TerminalSSH", message) }
}

private fun logError(message: String, throwable: Throwable) {
  runCatching { Log.e("TerminalSSH", message, throwable) }
}
