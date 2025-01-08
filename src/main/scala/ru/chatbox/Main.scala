package ru.chatbox

import akka.actor.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import ru.chatbox.actors.{DialogActor, OperatorActor}
import ru.chatbox.server.Controller

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.io.StdIn

class Main(clusterPort: String, serverPort: Int) extends App {
  val customConfig: Config = ConfigFactory.parseString(
    s"""
       |akka.remote.artery.canonical.port = $clusterPort
       |""".stripMargin).withFallback(ConfigFactory.load("application.conf"))

  implicit val system: ActorSystem = ActorSystem("DialogApp", customConfig)
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  val dialogSharding: ActorRef = DialogActor.initSharding(system)

  val operatorSharding: ClusterSharding = ClusterSharding(system.toTyped)
  OperatorActor.initSharding(operatorSharding, system.toTyped)

  val controller = new Controller(dialogSharding, operatorSharding)

  val server = Http().newServerAt("localhost", serverPort).bind(controller.route())

  StdIn.readLine()
  server.flatMap(_.unbind()).onComplete { _ => system.terminate() }
}

object Server1 extends Main("2551", 8080)
object Server2 extends Main("2552", 8081)
