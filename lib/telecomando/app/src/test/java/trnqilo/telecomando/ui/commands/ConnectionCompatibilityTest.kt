package trnqilo.telecomando.ui.commands

import org.junit.Assert.assertEquals
import org.junit.Test
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.ConnectionEntity

class ConnectionCompatibilityTest {
  private val ssh = ConnectionEntity(
    name = "Shell",
    address = "shell.example.test",
    username = "user",
    password = "",
    key = "",
    serverId = 1,
  )
  private val http = ConnectionEntity.http(
    name = "API",
    baseUrl = "https://api.example.test",
    connectionId = 2,
  )

  @Test
  fun filtersConnectionsForCommandType() {
    assertEquals(listOf(ssh), listOf(ssh, http).compatibleWith(CommandType.SSH.name))
    assertEquals(listOf(http), listOf(ssh, http).compatibleWith(CommandType.HTTP.name))
  }
}
