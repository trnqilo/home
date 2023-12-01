package trnqilo.telecomando.terminal

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import trnqilo.telecomando.ssh.JschSessionOpener

internal class JschTerminalSessionFactory : TerminalSessionFactory {
  override fun open(request: TerminalConnectionRequest): TerminalSession {
    val session = JschSessionOpener.open(request.connection)
    var channel: ChannelShell? = null
    var inputPipe: PipedInputStream? = null
    var inputWriter: PipedOutputStream? = null
    try {
      channel = session.openChannel("shell") as ChannelShell
      channel.setPty(true)
      channel.setPtyType("xterm")
      channel.setPtySize(DEFAULT_COLUMNS, DEFAULT_ROWS, 0, 0)
      inputPipe = PipedInputStream()
      inputWriter = PipedOutputStream(inputPipe)
      channel.setInputStream(inputPipe, true)
      channel.connect(CONNECT_TIMEOUT_MS)
      return JschTerminalSession(session, channel, inputWriter)
    } catch (exception: Exception) {
      inputWriter?.close()
      inputPipe?.close()
      channel?.disconnect()
      session.disconnect()
      throw exception
    }
  }

  private companion object {
    const val CONNECT_TIMEOUT_MS = 10_000
    const val DEFAULT_COLUMNS = 80
    const val DEFAULT_ROWS = 24
  }
}

private class JschTerminalSession(
  private val session: Session,
  private val shellChannel: ChannelShell,
  private val inputWriter: PipedOutputStream,
) : TerminalSession {
  private val closed = AtomicBoolean(false)
  private val writeBuffer = Any()

  override val events: Flow<TerminalEvent> = callbackFlow {
    val readerJob = launch(IO) {
      val stdout = InputStreamReader(shellChannel.inputStream)
      trySend(TerminalEvent.Connected())
      try {
        logDebug("Terminal session read loop started")
        while (!shellChannel.isClosed) {
          val emittedOutput = drain(stdout) { trySend(TerminalEvent.Output(it)) }
          if (!emittedOutput) delay(TERMINAL_POLL_INTERVAL_MS)
        }
        drain(stdout) { trySend(TerminalEvent.Output(it)) }
        trySend(TerminalEvent.Disconnected(shellChannel.exitStatus.takeIf { exitCode ->
          exitCode >= 0
        }))
      } catch (exception: CancellationException) {
        throw exception
      } catch (exception: Exception) {
        logError("Terminal session read loop failed", exception)
        trySend(TerminalEvent.Failed(exception.message ?: "An unknown exception occurred."))
      } finally {
        close()
      }
    }

    awaitClose {
      readerJob.cancel()
      close()
    }
  }

  override suspend fun sendInput(text: String) {
    withContext(IO) {
      if (closed.get()) return@withContext
      synchronized(writeBuffer) {
        inputWriter.write(text.toByteArray())
        inputWriter.flush()
        logDebug("Terminal input written length=${text.length}")
      }
    }
  }

  override fun resize(columns: Int, rows: Int) {
    if (!closed.get()) {
      shellChannel.setPtySize(columns, rows, 0, 0)
    }
  }

  override fun close() {
    if (closed.compareAndSet(false, true)) {
      logDebug("Closing terminal session")
      runCatching { inputWriter.close() }
      runCatching { shellChannel.disconnect() }
      runCatching { session.disconnect() }
    }
  }

  private inline fun drain(reader: InputStreamReader, emit: (String) -> Unit): Boolean {
    var emitted = false
    val buffer = CharArray(BUFFER_SIZE)
    while (reader.ready()) {
      val count = reader.read(buffer)
      if (count <= 0) break
      emit(buffer.concatToString(0, count))
      emitted = true
    }
    return emitted
  }

  private companion object {
    const val BUFFER_SIZE = 1_024
    const val TERMINAL_POLL_INTERVAL_MS = 25L
  }
}

private fun logDebug(message: String) {
  runCatching { android.util.Log.d("TerminalSSH", message) }
}

private fun logError(message: String, throwable: Throwable) {
  runCatching { android.util.Log.e("TerminalSSH", message, throwable) }
}
