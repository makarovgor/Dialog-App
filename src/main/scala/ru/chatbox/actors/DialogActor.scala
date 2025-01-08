package ru.chatbox.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import ru.chatbox.protocol.{AddReply, Dialog, DialogCommand, GetDialog, SendMessage}
import org.slf4j.LoggerFactory

class DialogActor extends Actor with ActorLogging {

  override def receive: Receive = behavior(Map.empty)

  private def behavior(dialogues: Map[String, List[String]]): Receive = {
    case SendMessage(userId, message) =>
      log.info(s"Received message from userId: $userId, message: $message")
      val updatedDialogues = dialogues.updatedWith(userId) {
        case Some(messages) => Some(messages :+ message)
        case None           => Some(List(message))
      }
      sender() ! Dialog(updatedDialogues(userId))
      context.become(behavior(updatedDialogues))

    case GetDialog(userId) =>
      log.info(s"Show dialog for userId: $userId")
      val messages = dialogues.getOrElse(userId, List.empty)
      sender() ! Dialog(messages)

    case AddReply(userId, name, reply) =>
      val updatedDialogues = dialogues.updatedWith(userId) {
        case Some(messages) => Some(messages :+ s"Operator $name: $reply")
        case None           => Some(List(s"Operator $name: $reply"))
      }
      context.become(behavior(updatedDialogues))
  }
}

object DialogActor {
  private val logger = LoggerFactory.getLogger(DialogActor.getClass)

  def props(): Props = Props(new DialogActor)

  val extractEntityId: ExtractEntityId = {
    case msg: DialogCommand => (msg.userId, msg)
  }

  val extractShardId: ExtractShardId = {
    case msg: DialogCommand =>
      val shardId = (msg.userId.hashCode % 10).toString
      logger.info(s"ExtractShardId for userId: ${msg.userId}, shardId: $shardId")
      shardId
  }

  def initSharding(system: ActorSystem): ActorRef = {
    ClusterSharding(system).start(
      typeName = "DialogActor",
      entityProps = DialogActor.props(),
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )
  }
}
