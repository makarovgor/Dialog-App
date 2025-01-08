package ru.chatbox.protocol

sealed trait DialogCommand {
  def userId: String
}

case class SendMessage(userId: String, message: String) extends DialogCommand
case class GetDialog(userId: String) extends DialogCommand
case class AddReply(userId: String, name: String, reply: String) extends DialogCommand

case class Dialog(messages: List[String])
