package actor


import akka.actor._
import akka.event._
import scala.concurrent.duration._
import scala.util._

import actor.Testing._
import models.database._
import actor.Testing.GetState
import akka.util.Timeout

class CrawlRequestActor(val pageFetchActor: ActorRef, val databaseActor: ActorRef) extends Actor with UnboundedStash with ActorLogging {
  import context._

  val receive = idle

  override def preStart() = {
  }

  override def postStop() = {
  }

  def idle: Receive = LoggingReceive {
    case (request: CrawlRequest) => {
      log.debug(s"Request received.  Becoming initializing.")
      context.setReceiveTimeout(10.seconds)
      become(initializing)
      unstashAll
      databaseActor ! CreateEntity(request, CrawlRequestQuery)
    }

    case GetState() => sender ! Idle()
  }

  def initializing: Receive = LoggingReceive {
    case Created(response) => {
      response match {
        case Success(request) => {
          log.debug(s"Initialization complete.  Becoming processing.")
          //become(processing(request, List.empty))
          unstashAll
          context.setReceiveTimeout(10.seconds)
          //self ! PageFetchRequest(None, request.id, request.origin, None, None, request.initialRecursionLevel, request.includeExternalLinks)
        }
        case Failure(ex) => {
          log.debug(s"Initialization failed.  Becoming idle.")
          context.setReceiveTimeout(Duration.Undefined)
          become(idle)
          unstashAll()
        }
      }
    }
    case ReceiveTimeout => {
      log.debug(s"Initialization timed out.  Becoming idle.")
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
      unstashAll()
    }

    case GetState() => sender ! Initializing()
    case _ => stash
  }

  def processing(crawlRequest: CrawlRequest, history: List[String]): Receive = LoggingReceive {
    case PageFetchSuccess(request) => {
      databaseActor ! UpdateEntities((row: PageFetchRequests) => row.id == request.id, (_: PageFetchRequests) => request, PageFetchRequestQuery)
    }
    case PageFetchFailure(request, ex) => {
      log.debug("Page fetch failure received.")
    }
    case (request: PageFetchRequest) => {
      implicit val timeout = Timeout(5.seconds)
      log.debug("Child page fetch request received.")
      if (!history.contains(request.url)) {
        become(processing(crawlRequest, history :+ request.url))
        databaseActor ! CreateEntity(request, PageFetchRequestQuery)
      }
    }

    case Created(Success(pageFetchRequest)) => {
      pageFetchActor ! pageFetchRequest
    }
    case Created(Failure(ex)) => {
      //TODO Handle failure
    }

    case Updated(Success(pageFetchRequests)) => {
      //log.debug(s"Successfully updated ${pageFetchRequests.map(request => request.id)}}")
    }case Updated(Failure(ex)) => {
      //TODO: Handle failure
    }

    case GetState() => sender ! Processing()
    case ReceiveTimeout => {
      log.debug("No activity received for 10 seconds.  Becoming idle.")
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
      unstashAll
    }
    case _ => stash()
  }
}


