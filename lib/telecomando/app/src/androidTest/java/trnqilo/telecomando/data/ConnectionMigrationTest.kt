package trnqilo.telecomando.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionMigrationTest {
  @get:Rule
  val helper = MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDatabase::class.java,
    emptyList(),
    FrameworkSQLiteOpenHelperFactory(),
  )

  @Test
  fun migratesSshServersAndInlineHttpCommands() {
    helper.createDatabase(TEST_DATABASE, 8).apply {
      execSQL(
        "INSERT INTO servers(serverId,name,address,port,username,password,`key`) VALUES (1,'Shell','host.test',2222,'user','secret','key')"
      )
      execSQL(
        "INSERT INTO commands(commandId,name,command,serverIds,type,configJson) VALUES (10,'Uptime','uptime','1','SSH','{}')"
      )
      execSQL("INSERT INTO CommandServerCrossRef(commandId,serverId) VALUES (10,1)")
      execSQL(
        "INSERT INTO commands(commandId,name,command,serverIds,type,configJson) VALUES (11,'Health','','','HTTP','{\"url\":\"https://api.test/health\",\"method\":\"GET\",\"headersJson\":\"{}\"}')"
      )
      close()
    }

    val database = helper.runMigrationsAndValidate(TEST_DATABASE, 9, true, MIGRATION_8_9)

    database.query("SELECT type,configJson,credentialJson FROM connections WHERE connectionId=1").use { cursor ->
      cursor.moveToFirst()
      assertEquals(ConnectionType.SSH.name, cursor.getString(0))
      assertEquals("host.test", cursor.getString(1).toSshConnectionConfig().address)
      assertEquals("secret", cursor.getString(2).toConnectionCredentials().password)
    }
    database.query("SELECT connectionId,configJson FROM connections WHERE type='HTTP'").use { cursor ->
      cursor.moveToFirst()
      val connectionId = cursor.getInt(0)
      assertEquals("https://api.test/health", cursor.getString(1).toHttpConnectionConfig().baseUrl)
      database.query("SELECT configJson FROM commands WHERE commandId=11").use { commandCursor ->
        commandCursor.moveToFirst()
        assertEquals("", commandCursor.getString(0).toHttpCommandConfig().path)
      }
      database.query(
        "SELECT COUNT(*) FROM command_connection_cross_ref WHERE commandId=11 AND connectionId=$connectionId"
      ).use { relationCursor ->
        relationCursor.moveToFirst()
        assertEquals(1, relationCursor.getInt(0))
      }
    }
    database.close()
  }

  private companion object {
    const val TEST_DATABASE = "connections-migration-test"
  }
}
