package trnqilo.telecomando.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
  tableName = "command_connection_cross_ref",
  primaryKeys = ["commandId", "connectionId"],
  indices = [Index("commandId"), Index("connectionId")]
)
data class CommandConnectionCrossRef(
  val commandId: Int,
  val connectionId: Int,
)

typealias CommandServerCrossRef = CommandConnectionCrossRef
