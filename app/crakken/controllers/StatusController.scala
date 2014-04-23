package crakken.controllers


import akka.pattern.ask
import akka.util.Timeout
import crakken.Global
import crakken.actor.StatusActor
import crakken.actor.StatusActor.Subscribed
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Akka
import play.api.mvc.{WebSocket, Controller}
import play.api.Play.current
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import ExecutionContext.Implicits.global

import scala.language.postfixOps

object StatusController extends Controller {
  implicit val timeout = Timeout(10 seconds)

  val statusActor = Akka.system.actorSelection(Global.statusUpdateRouterPathName)

  def status(channelName: String) = WebSocket.async[JsValue] { request =>
    (statusActor ? StatusActor.Subscribe(channelName)).map {
      case Subscribed(out) => {
        val in = Iteratee.foreach[JsValue] { event =>  }
        (in, out)
      }
    }
  }
}
