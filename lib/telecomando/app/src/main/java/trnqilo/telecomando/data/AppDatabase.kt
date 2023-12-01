package trnqilo.telecomando.data

import android.content.Context
import android.content.ContentValues
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    ConnectionEntity::class,
    CommandEntity::class,
    CommandConnectionCrossRef::class,
    AutomationRuleEntity::class,
    AutomationInitiatorEntity::class,
    AutomationStepEntity::class,
  ],
  version = 9
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun commandDao(): CommandServerDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE servers ADD COLUMN port INTEGER NOT NULL DEFAULT 22")
  }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS automation_rules (
        ruleId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT NOT NULL,
        enabled INTEGER NOT NULL,
        initiatorType TEXT NOT NULL,
        initiatorDelayMinutes INTEGER NOT NULL,
        commandId INTEGER NOT NULL,
        completionPolicy TEXT NOT NULL
      )
      """.trimIndent()
    )
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS automation_actions (
        actionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        ruleId INTEGER NOT NULL,
        orderIndex INTEGER NOT NULL,
        type TEXT NOT NULL,
        configJson TEXT NOT NULL,
        FOREIGN KEY(ruleId) REFERENCES automation_rules(ruleId) ON DELETE CASCADE
      )
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS index_automation_actions_ruleId ON automation_actions(ruleId)")
  }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE IF EXISTS automation_actions")
  }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE commands ADD COLUMN type TEXT NOT NULL DEFAULT 'SSH'")
    db.execSQL("ALTER TABLE commands ADD COLUMN configJson TEXT NOT NULL DEFAULT '{}'")
  }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS automation_steps (
        stepId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        ruleId INTEGER NOT NULL,
        orderIndex INTEGER NOT NULL,
        commandId INTEGER NOT NULL,
        successNextStepId INTEGER,
        failureNextStepId INTEGER
      )
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS index_automation_steps_ruleId ON automation_steps(ruleId)")
    db.execSQL(
      """
      INSERT INTO automation_steps(ruleId, orderIndex, commandId, successNextStepId, failureNextStepId)
      SELECT ruleId, 0, commandId, NULL, NULL
      FROM automation_rules
      """.trimIndent()
    )
  }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS automation_steps_new (
        stepId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        ruleId INTEGER NOT NULL,
        orderIndex INTEGER NOT NULL,
        commandId INTEGER NOT NULL,
        successNextStepId INTEGER,
        failureNextStepId INTEGER
      )
      """.trimIndent()
    )
    db.execSQL(
      """
      INSERT INTO automation_steps_new(stepId, ruleId, orderIndex, commandId, successNextStepId, failureNextStepId)
      SELECT stepId, ruleId, orderIndex, commandId, successNextStepId, failureNextStepId
      FROM automation_steps
      """.trimIndent()
    )
    db.execSQL("DROP TABLE automation_steps")
    db.execSQL("ALTER TABLE automation_steps_new RENAME TO automation_steps")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_automation_steps_ruleId ON automation_steps(ruleId)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_automation_steps_ruleId_orderIndex ON automation_steps(ruleId, orderIndex)")
  }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE IF EXISTS automation_initiators")
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS automation_initiators (
        initiatorId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        ruleId INTEGER NOT NULL,
        orderIndex INTEGER NOT NULL,
        type TEXT NOT NULL,
        configJson TEXT NOT NULL
      )
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS index_automation_initiators_ruleId ON automation_initiators(ruleId)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_automation_initiators_ruleId_orderIndex ON automation_initiators(ruleId, orderIndex)")
    db.execSQL(
      """
      INSERT INTO automation_initiators(ruleId, orderIndex, type, configJson)
      SELECT ruleId, 0, initiatorType,
        CASE
          WHEN initiatorType = 'Alarm' THEN '{"delayMinutes":"' || initiatorDelayMinutes || '"}'
          ELSE '{}'
        END
      FROM automation_rules
      """.trimIndent()
    )
  }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS connections (
        name TEXT NOT NULL,
        type TEXT NOT NULL,
        configJson TEXT NOT NULL,
        credentialJson TEXT NOT NULL,
        connectionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
      )
      """.trimIndent()
    )
    db.query("SELECT serverId, name, address, port, username, password, `key` FROM servers").use { cursor ->
      while (cursor.moveToNext()) {
        val values = ContentValues().apply {
          put("connectionId", cursor.getInt(0))
          put("name", cursor.getString(1))
          put("type", ConnectionType.SSH.name)
          put("configJson", SshConnectionConfig(cursor.getString(2), cursor.getInt(3), cursor.getString(4)).toJson())
          put("credentialJson", ConnectionCredentials(cursor.getString(5), cursor.getString(6)).toJson())
        }
        db.insert("connections", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
      }
    }

    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS command_connection_cross_ref (
        commandId INTEGER NOT NULL,
        connectionId INTEGER NOT NULL,
        PRIMARY KEY(commandId, connectionId)
      )
      """.trimIndent()
    )
    db.execSQL(
      """
      INSERT INTO command_connection_cross_ref(commandId, connectionId)
      SELECT commandId, serverId FROM CommandServerCrossRef
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS index_command_connection_cross_ref_commandId ON command_connection_cross_ref(commandId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_command_connection_cross_ref_connectionId ON command_connection_cross_ref(connectionId)")

    var nextConnectionId = db.query("SELECT COALESCE(MAX(connectionId), 0) FROM connections").use { cursor ->
      cursor.moveToFirst()
      cursor.getInt(0) + 1
    }
    db.query("SELECT commandId, name, configJson FROM commands WHERE type = 'HTTP'").use { cursor ->
      while (cursor.moveToNext()) {
        val commandId = cursor.getInt(0)
        val commandName = cursor.getString(1)
        val commandConfig = cursor.getString(2).toHttpCommandConfig()
        val connectionId = nextConnectionId++
        val connectionValues = ContentValues().apply {
          put("connectionId", connectionId)
          put("name", "$commandName endpoint")
          put("type", ConnectionType.HTTP.name)
          put("configJson", HttpConnectionConfig(commandConfig.path).toJson())
          put("credentialJson", "{}")
        }
        db.insert("connections", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, connectionValues)
        db.execSQL(
          "INSERT INTO command_connection_cross_ref(commandId, connectionId) VALUES (?, ?)",
          arrayOf(commandId, connectionId),
        )
        val updatedCommandConfig = commandConfig.copy(path = "").toJson()
        db.execSQL(
          "UPDATE commands SET serverIds = ?, configJson = ? WHERE commandId = ?",
          arrayOf<Any>(connectionId.toString(), updatedCommandConfig, commandId),
        )
      }
    }

    db.execSQL("DROP TABLE CommandServerCrossRef")
    db.execSQL("DROP TABLE servers")
  }
}

object DatabaseBuilder {
  private var instance: AppDatabase? = null
  fun appDatabase(context: Context): AppDatabase {
    if (instance == null) {
      synchronized(AppDatabase::class) {
        instance = databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "app_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9).build()
      }
    }
    return instance ?: throw Exception("Database could not be built")
  }
}
