package trnqilo.telecomando.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CommandWithServers(
  @Embedded val command: CommandEntity,
  @Relation(
    parentColumn = "commandId",
    entityColumn = "connectionId",
    associateBy = Junction(CommandConnectionCrossRef::class)
  )
  val servers: List<ConnectionEntity>
)

typealias CommandWithConnections = CommandWithServers
