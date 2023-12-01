package trnqilo.telecomando.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionEntityTest {
  @Test
  fun roundTripsSshConfigurationAndCredentials() {
    val connection = ConnectionEntity(
      name = "Local",
      address = "127.0.0.1",
      port = 1234,
      username = "user",
      password = "password",
      key = "private key",
      serverId = 7,
    )

    assertEquals(ConnectionType.SSH.name, connection.type)
    assertEquals("127.0.0.1", connection.address)
    assertEquals(1234, connection.port)
    assertEquals("user", connection.username)
    assertEquals("password", connection.password)
    assertEquals("private key", connection.key)
    assertEquals(7, connection.connectionId)
  }

  @Test
  fun roundTripsHttpConfiguration() {
    val connection = ConnectionEntity.http(
      name = "Production",
      baseUrl = "https://api.example.test",
      defaultHeadersJson = "{\"Accept\":\"application/json\"}",
      connectionId = 8,
    )

    assertEquals(ConnectionType.HTTP.name, connection.type)
    assertEquals(
      HttpConnectionConfig(
        baseUrl = "https://api.example.test",
        defaultHeadersJson = "{\"Accept\":\"application/json\"}",
      ),
      connection.configJson.toHttpConnectionConfig(),
    )
  }
}
