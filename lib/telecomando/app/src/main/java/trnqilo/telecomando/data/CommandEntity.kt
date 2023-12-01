package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class CommandEntity(
  var name: String,
  var command: String,
  var serverIds: String,
  var type: String = CommandType.SSH.name,
  var configJson: String = "{}",
  @PrimaryKey(autoGenerate = true) val commandId: Int = 0,
)
