package trnqilo.telecomando.command

import com.jcraft.jsch.ChannelExec
import java.io.InputStream
import trnqilo.telecomando.ssh.JschSessionOpener

internal fun interface CommandConnectionFactory {
  fun open(request: SshCommandRequest): CommandConnection
}

internal interface CommandConnection : AutoCloseable {
  val stdout: InputStream
  val stderr: InputStream
  val isClosed: Boolean
  val exitStatus: Int
}

internal class JschCommandConnectionFactory : CommandConnectionFactory {
  override fun open(request: SshCommandRequest): CommandConnection {
    val session = JschSessionOpener.open(request.toConnectionRequest())
    var channel: ChannelExec? = null
    try {
      channel = session.openChannel("exec") as ChannelExec
      channel.setCommand(request.command)
      val stdout = channel.inputStream
      val stderr = channel.errStream
      channel.connect(CONNECT_TIMEOUT_MS)
      return JschCommandConnection(session, channel, stdout, stderr)
    } catch (exception: Exception) {
      channel?.disconnect()
      session.disconnect()
      throw exception
    }
  }

  private companion object {
    const val CONNECT_TIMEOUT_MS = 10_000
  }
}

private class JschCommandConnection(
  private val session: com.jcraft.jsch.Session,
  private val channel: ChannelExec,
  override val stdout: InputStream,
  override val stderr: InputStream,
) : CommandConnection {
  override val isClosed: Boolean
    get() = channel.isClosed

  override val exitStatus: Int
    get() = channel.exitStatus

  override fun close() {
    channel.disconnect()
    session.disconnect()
  }
}
