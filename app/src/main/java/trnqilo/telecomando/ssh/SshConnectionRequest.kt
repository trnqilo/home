package trnqilo.telecomando.ssh

data class SshConnectionRequest(
  val destination: String,
  val port: Int = 22,
  val user: String = "",
  val password: String = "",
)
