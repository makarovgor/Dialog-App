package ru.chatbox.protocol

case class MessageRequest(userId: String, message: String)
case class ReplyRequest(userId: String, name: String, reply: String)
case class DialogResponse(userId: String, messages: List[String])
