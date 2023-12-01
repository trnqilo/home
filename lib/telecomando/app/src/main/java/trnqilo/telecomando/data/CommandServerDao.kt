package trnqilo.telecomando.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandServerDao {
  @Insert(onConflict = REPLACE)
  suspend fun insertCommand(command: CommandEntity): Long

  @Insert(onConflict = REPLACE)
  suspend fun insertServer(server: ConnectionEntity): Long

  @Insert(onConflict = REPLACE)
  suspend fun insertCommandServerCrossRef(crossRef: CommandServerCrossRef)

  @Delete
  suspend fun deleteCommandServerCrossRef(crossRef: CommandServerCrossRef)

  @Query("SELECT * FROM connections")
  fun getServers(): Flow<List<ConnectionEntity>>

  @Transaction
  @Query("SELECT * FROM commands")
  fun getCommandsWithServers(): Flow<List<CommandWithServers>>

  @Query("SELECT * FROM commands")
  fun getCommands(): Flow<List<CommandEntity>>

  @Transaction
  @Query("SELECT * FROM commands WHERE commandId = :commandId LIMIT 1")
  suspend fun getCommandWithServers(commandId: Int): CommandWithServers?

  @Insert(onConflict = REPLACE)
  suspend fun insertAutomationRule(rule: AutomationRuleEntity): Long

  @Insert(onConflict = REPLACE)
  suspend fun insertAutomationInitiator(initiator: AutomationInitiatorEntity): Long

  @Insert(onConflict = REPLACE)
  suspend fun insertAutomationStep(step: AutomationStepEntity): Long

  @Query(
    """
    UPDATE automation_steps
    SET successNextStepId = :successNextStepId,
        failureNextStepId = :failureNextStepId
    WHERE stepId = :stepId
    """
  )
  suspend fun updateAutomationStepLinks(
    stepId: Int,
    successNextStepId: Int?,
    failureNextStepId: Int?,
  )

  @Query("DELETE FROM automation_steps WHERE ruleId = :ruleId")
  suspend fun deleteAutomationSteps(ruleId: Int)

  @Query("DELETE FROM automation_initiators WHERE ruleId = :ruleId")
  suspend fun deleteAutomationInitiators(ruleId: Int)

  @Query("DELETE FROM automation_rules WHERE ruleId = :ruleId")
  suspend fun deleteAutomationRule(ruleId: Int)

  @Transaction
  @Query("SELECT * FROM automation_rules")
  fun getAutomationRules(): Flow<List<AutomationRuleEntity>>

  @Transaction
  @Query("SELECT * FROM automation_rules")
  fun getAutomationRulesWithSteps(): Flow<List<AutomationRuleWithSteps>>

  @Transaction
  @Query("SELECT * FROM automation_rules WHERE ruleId = :ruleId LIMIT 1")
  suspend fun getAutomationRule(ruleId: Int): AutomationRuleWithSteps?

  @Transaction
  suspend fun deleteAutomation(ruleId: Int) {
    deleteAutomationSteps(ruleId)
    deleteAutomationRule(ruleId)
  }

}
