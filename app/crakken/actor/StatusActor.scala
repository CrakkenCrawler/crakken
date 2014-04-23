package crakken.actor

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json.JsValue
import play.api.libs.iteratee.{Enumerator, Concurrent}

class StatusActor extends Actor with ActorLogging {
  val receive = accept(Map.empty)

  val (statusEnumerator, statusChannel) = Concurrent.broadcast[JsValue]

  def accept(channels: Map[String,(Enumerator[JsValue], Concurrent.Channel[JsValue])]) : Receive = LoggingReceive {
    case StatusActor.Subscribe(channelName) => {
      channels.get(channelName) match {
        case Some((enumerator, channel)) => sender ! StatusActor.Subscribed(enumerator)
        case None => {
          val (enumerator, channel) = Concurrent.broadcast[JsValue]
          context.become(accept(channels + (channelName -> (enumerator, channel))))
          sender ! StatusActor.Subscribed(enumerator)
        }
      }
    }
    case StatusActor.Update(channelName, updatedObject) => channels.get(channelName) match {
      case Some((enumerator, channel)) => channel.push(updatedObject)
      case None => {}
    }
    case StatusActor.Complete(channelName) => channels.get(channelName) match {
      case Some(_) => context.become(accept(channels - channelName))
      case None => {}
    }
  }
}

object StatusActor {
  def props = Props { new StatusActor() }

  case class Subscribe(channelName: String)
  case class Subscribed(enumerator: Enumerator[JsValue])
  case class Update(channelName: String, updatedObject: JsValue)
  case class Complete(channelName: String)
}
