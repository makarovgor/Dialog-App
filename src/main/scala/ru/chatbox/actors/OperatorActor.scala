package ru.chatbox.actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.{ClusterShardingSettings, HashCodeMessageExtractor}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import ru.chatbox.protocol.{AddReply, OperatorCommand, OperatorReply}

object OperatorActor {

  val TypeKey: EntityTypeKey[OperatorCommand] = EntityTypeKey[OperatorCommand]("OperatorActor")

  def apply(): Behavior[OperatorCommand] = Behaviors.receive { (context, message) =>
    message match {
      case OperatorReply(userId, name, reply, dialogActor) =>
        context.log.info(s"Replying to $userId: $reply")
        dialogActor ! AddReply(userId, name, reply)
        Behaviors.same
    }
  }

  def initSharding(sharding: ClusterSharding, system: ActorSystem[Nothing]): Unit = {
    val numberOfShards = 10

    val messageExtractor = new HashCodeMessageExtractor[OperatorCommand](numberOfShards)

    sharding.init(
      Entity(OperatorActor.TypeKey)(createBehavior = _ => OperatorActor())
        .withSettings(ClusterShardingSettings(system))
        .withMessageExtractor(messageExtractor)
    )
  }

}
