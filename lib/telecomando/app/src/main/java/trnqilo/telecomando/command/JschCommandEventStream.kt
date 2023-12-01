package trnqilo.telecomando.command

import java.io.InputStreamReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal class JschCommandEventStream(
  private val connectionFactory: CommandConnectionFactory = JschCommandConnectionFactory(),
) : CommandEventStream {
  override fun execute(request: SshCommandRequest): Flow<CommandEvent> = callbackFlow {
    val execution = launch(IO) {
      var connection: CommandConnection? = null
      try {
        connection = connectionFactory.open(request)
        val stdout = InputStreamReader(connection.stdout)
        val stderr = InputStreamReader(connection.stderr)

        while (!connection.isClosed) {
          val emittedOutput = drain(stdout) { trySend(CommandEvent.Stdout(it)) } or
            drain(stderr) { trySend(CommandEvent.Stderr(it)) }
          if (!emittedOutput) delay(OUTPUT_POLL_INTERVAL_MS)
        }

        drain(stdout) { trySend(CommandEvent.Stdout(it)) }
        drain(stderr) { trySend(CommandEvent.Stderr(it)) }
        trySend(CommandEvent.Completed(connection.exitStatus))
      } catch (exception: CancellationException) {
        throw exception
      } catch (exception: Exception) {
        trySend(CommandEvent.Failed(exception.message ?: "An unknown exception occurred."))
      } finally {
        connection?.close()
        close()
      }
    }

    awaitClose { execution.cancel() }
  }

  private inline fun drain(reader: InputStreamReader, emit: (String) -> Unit): Boolean {
    var emitted = false
    val buffer = CharArray(OUTPUT_BUFFER_SIZE)
    while (reader.ready()) {
      val count = reader.read(buffer)
      if (count <= 0) break
      emit(buffer.concatToString(0, count))
      emitted = true
    }
    return emitted
  }

  private companion object {
    const val OUTPUT_BUFFER_SIZE = 1_024
    const val OUTPUT_POLL_INTERVAL_MS = 25L
  }
}
