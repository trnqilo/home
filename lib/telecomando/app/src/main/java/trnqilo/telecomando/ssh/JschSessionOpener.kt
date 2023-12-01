package trnqilo.telecomando.ssh

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.util.Properties

internal object JschSessionOpener {
  fun open(request: SshConnectionRequest): Session {
    val session = JSch().getSession(request.user, request.destination, request.port)
    session.setPassword(request.password)
    session.setConfig(Properties().apply { this["StrictHostKeyChecking"] = "no" })
    session.connect(CONNECT_TIMEOUT_MS)
    return session
  }

  private const val CONNECT_TIMEOUT_MS = 10_000
}
