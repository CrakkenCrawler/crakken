package crakken.actor


import akka.actor._
import akka.event._
import scala.concurrent.duration._
import akka.util.Timeout

import scala.reflect._
import crakken.data.repository._
import crakken.actor.Testing.Processing
import crakken.actor.Testing.GetState
import crakken.actor.Testing.Idle
import crakken.data.model.PageFetchRequest
import crakken.data.model.CrawlRequest
import play.api.libs.json.Json

object CrawlRequestActor {
  def props(pageFetchActor: ActorRef, repositoryActor: ActorRef, statusActor: ActorRef) : Props = Props(new CrawlRequestActor(pageFetchActor, repositoryActor, statusActor))
}

class CrawlRequestActor(pageFetchActor: ActorRef, repositoryActor: ActorRef, statusActor: ActorRef) extends Actor with UnboundedStash with ActorLogging {
  import context._

  val receive = idle

  override def preStart() = {
  }

  override def postStop() = {
  }

  def idle: Receive = LoggingReceive {
    case (request: CrawlRequest) => {
      log.debug(s"Request received.  Becoming processing.")
      context.setReceiveTimeout(10.seconds)
      become(processing(request, List.empty))
      unstashAll()
      repositoryActor ! CrawlRequestMessages.create(request)
      statusActor ! StatusActor.Update("crawlrequest", Json.toJson(request))
      self ! PageFetchRequest(None, request.id , request.origin, None, None, request.initialRecursionLevel, request.includeExternalLinks)
    }

    case GetState() => sender ! Idle()
    case _ => stash()

  }

  def processing(crawlRequest: CrawlRequest, history: List[String]): Receive = LoggingReceive {

    case PageFetchSuccess(request) => {
      log.info(s"Page fetch for ${request.url} completed with code ${request.statusCode.getOrElse("unknown")}.")
      statusActor ! StatusActor.Update(s"crawlrequest/${request.crawlRequestId}", Json.toJson(request))
      repositoryActor ! PageFetchRequestMessages.update(request)
    }
    case PageFetchFailure(request, ex) => {
      statusActor ! StatusActor.Update(s"crawlrequest/${request.crawlRequestId}", Json.toJson(request))
      log.error(ex, "Page fetch failure received.")
    }
    case (request: PageFetchRequest) => {
      log.info(s"Page fetch for ${request.url} received.")
      if (!history.contains(request.url)) {
        become(processing(crawlRequest, history :+ request.url))
        statusActor ! StatusActor.Update(s"crawlrequest/${request.crawlRequestId}", Json.toJson(request))
        repositoryActor ! PageFetchRequestMessages.create(request)
        pageFetchActor ! request
      }
    }

    case GetState() => sender ! Processing()
    case ReceiveTimeout => {
      statusActor ! StatusActor.Complete(s"crawlrequest/${crawlRequest.id}")
      statusActor ! StatusActor.Update(s"crawlrequest", Json.toJson(crawlRequest))
      log.debug("No activity received for 10 seconds.  Becoming idle.")
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
      unstashAll()
    }
    case _ => stash()
  }
}


