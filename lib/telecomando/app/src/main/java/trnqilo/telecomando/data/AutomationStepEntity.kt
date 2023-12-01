package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Index
import androidx.room.Relation
import androidx.room.PrimaryKey

@Entity(
  tableName = "automation_steps",
  indices = [
    Index("ruleId"),
    Index(value = ["ruleId", "orderIndex"], unique = true),
  ],
)
data class AutomationStepEntity(
  val ruleId: Int,
  val orderIndex: Int,
  val commandId: Int,
  val successNextStepId: Int? = null,
  val failureNextStepId: Int? = null,
  @PrimaryKey(autoGenerate = true) val stepId: Int = 0,
)

data class AutomationRuleWithSteps(
  @Embedded val rule: AutomationRuleEntity,
  @Relation(parentColumn = "ruleId", entityColumn = "ruleId")
  val initiators: List<AutomationInitiatorEntity>,
  @Relation(parentColumn = "ruleId", entityColumn = "ruleId")
  val steps: List<AutomationStepEntity>,
)
