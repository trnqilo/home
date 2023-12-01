package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
  val name: String,
  val enabled: Boolean,
  val initiatorType: String,
  val initiatorDelayMinutes: Int,
  val commandId: Int,
  val completionPolicy: String,
  @PrimaryKey(autoGenerate = true) val ruleId: Int = 0,
)
