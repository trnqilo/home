import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import trnqilo.telecomando.data.AppDatabase
import trnqilo.telecomando.data.AppRepo
import trnqilo.telecomando.data.CommandEntity
import trnqilo.telecomando.data.CommandWithServers
import trnqilo.telecomando.data.ServerEntity

@RunWith(AndroidJUnit4::class)
class AppRepoTest {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var db: AppDatabase
  private lateinit var repo: AppRepo

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repo = AppRepo(db.commandDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun addServerWillPersistServerData() = runBlocking {
    val server = buildServer()

    val servers: List<ServerEntity> = repo.getServers().first()
    assertEquals(1, servers.size)
    assertEquals(listOf(server), servers)
  }

  @Test
  fun addCommandWillPersistCommandData() = runBlocking {
    val server = buildServer()

    val command = CommandEntity(name = "wasd", command = "ls", serverIds = "nothing", commandId = 321)
    repo.addCommand(CommandWithServers(command, listOf(server)))

    val commandWithServersList = repo.getCommands().first()
    val commandWithServers = commandWithServersList.first()
    assertEquals(1, commandWithServersList.size)
    assertEquals(command, commandWithServers.command)
    assertEquals(1, commandWithServers.servers.size)
    assertEquals(listOf(server), commandWithServers.servers)
  }

  private suspend fun buildServer(): ServerEntity {
    val server = ServerEntity(
      name = "Test Server",
      address = "127.0.0.1",
      port = 1234,
      username = "admin",
      password = "password",
      key = "key",
      serverId = 123
    )
    repo.addServer(server)
    return server
  }
}
