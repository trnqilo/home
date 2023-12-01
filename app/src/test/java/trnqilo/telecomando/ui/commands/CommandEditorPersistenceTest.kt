package trnqilo.telecomando.ui.commands

import org.junit.Assert.assertEquals
import org.junit.Test
import trnqilo.telecomando.commands.CommandDraft
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandType
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.HttpMethod
import trnqilo.telecomando.data.ConnectionEntity
import trnqilo.telecomando.data.ServerEntity

class CommandEditorPersistenceTest {
  private val first = server(1)
  private val second = server(2)

  @Test
  fun createsDraftInPersistedServerOrderAndIgnoresInvalidIds() {
    val model = CommandWithServers(
      CommandEntity("Status", "uptime", "2,invalid,1", CommandType.SSH.name, "{}", 10),
      listOf(first, second),
    )

    assertEquals(
      CommandDraft("Status", "uptime", listOf(2, 1)),
      model.toCommandDraft(),
    )
  }

  @Test
  fun createsSaveInDraftOrderAndCalculatesRemovedServers() {
    val original = CommandWithServers(
      CommandEntity("Old", "old", "1,2", CommandType.SSH.name, "{}", 10),
      listOf(first, second),
    )
    val draft = CommandDraft("New", "new", listOf(2))

    val save = draft.toCommandSave(original, listOf(first, second))

    assertEquals("2", save.command.command.serverIds)
    assertEquals(listOf(second), save.command.servers)
    assertEquals(listOf(first), save.removedServers)
  }

  @Test
  fun roundTripsBodylessHttpCommandConfiguration() {
    val draft = CommandDraft(
      name = "Health",
      type = CommandType.HTTP.name,
      httpPath = "https://example.test/health",
      httpMethod = HttpMethod.GET.name,
      httpHeadersJson = "{\"Accept\":\"application/json\"}",
    )

    val saved = draft.toCommandSave(original = null, allServers = emptyList()).command

    assertEquals(draft, saved.toCommandDraft())
  }

  @Test
  fun roundTripsHttpBodyAndEscapedHeaders() {
    val draft = CommandDraft(
      name = "Create",
      command = "{\"message\":\"{{result}}\"}",
      type = CommandType.HTTP.name,
      httpPath = "https://example.test/items",
      httpMethod = HttpMethod.POST.name,
      httpHeadersJson = "{\"Authorization\":\"Bearer abc\",\"X-Note\":\"a\\\"b\"}",
    )

    val saved = draft.toCommandSave(original = null, allServers = emptyList()).command

    assertEquals(draft, saved.toCommandDraft())
  }

  @Test
  fun persistsSelectedHttpConnections() {
    val connection = ConnectionEntity.http(
      name = "Production",
      baseUrl = "https://api.example.test",
      connectionId = 3,
    )
    val draft = CommandDraft(
      name = "Health",
      type = CommandType.HTTP.name,
      selectedServerIds = listOf(3),
      httpPath = "/health",
    )

    val saved = draft.toCommandSave(original = null, allServers = listOf(connection)).command

    assertEquals(listOf(connection), saved.servers)
    assertEquals("3", saved.command.serverIds)
  }

  private fun server(id: Int) = ServerEntity(
    name = "Server $id",
    address = "server$id.test",
    port = 2200 + id,
    username = "user",
    password = "password",
    key = "",
    serverId = id,
  )
}
