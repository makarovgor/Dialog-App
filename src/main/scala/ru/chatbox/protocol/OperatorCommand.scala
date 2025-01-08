package ru.chatbox.protocol

import akka.actor.ActorRef

sealed trait OperatorCommand
case class OperatorReply(userId: String, name: String, reply: String, dialogActor: ActorRef) extends OperatorCommand
