package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "automation_initiators",
  indices = [
    Index("ruleId"),
    Index(value = ["ruleId", "orderIndex"], unique = true),
  ],
)
data class AutomationInitiatorEntity(
  val ruleId: Int,
  val orderIndex: Int,
  val type: String,
  val configJson: String,
  @PrimaryKey(autoGenerate = true) val initiatorId: Int = 0,
)
