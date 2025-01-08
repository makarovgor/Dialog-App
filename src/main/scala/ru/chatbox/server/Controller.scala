package ru.chatbox.server

import akka.actor.ActorRef
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import ru.chatbox.actors.OperatorActor
import ru.chatbox.protocol.{Dialog, DialogResponse, GetDialog, MessageRequest, OperatorCommand, OperatorReply, ReplyRequest, SendMessage}

import scala.concurrent.Future

class Controller(dialogSharding: ActorRef,
                 operatorSharding: ClusterSharding)
                (implicit timeout: Timeout) {

  def route(): Route = {
    pathPrefix("api") {
      path("sendMessage") {
        sendMessage()
      } ~
      path("getDialog" / Segment) {
        getDialog
      } ~
      path("reply") {
        reply()
      }
    }
  }

  private def sendMessage(): Route = {
    post {
      entity(as[MessageRequest]) { request =>
        val response: Future[Dialog] = dialogSharding.ask(SendMessage(request.userId, request.message)).mapTo[Dialog]
        onSuccess(response) { messages =>
          complete(StatusCodes.OK, DialogResponse(request.userId, messages.messages).asJson)
        }
      }
    }
  }

  private def getDialog(userId: String): Route = {
    get {
      val response: Future[Dialog] = dialogSharding.ask(GetDialog(userId)).mapTo[Dialog]
      onSuccess(response) { messages =>
        complete(StatusCodes.OK, DialogResponse(userId, messages.messages).asJson)
      }
    }
  }

    private def reply(): Route = {
      (post & entity(as[ReplyRequest])) { request =>
        val operatorActor: EntityRef[OperatorCommand] = operatorSharding.entityRefFor(OperatorActor.TypeKey, request.userId)
        operatorActor.tell(OperatorReply(request.userId, request.name, request.reply, dialogSharding))
        complete(StatusCodes.OK)
      }
    }

}
