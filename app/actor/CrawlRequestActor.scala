package actor


import akka.actor._
import akka.event._
import scala.concurrent.duration._
import scala.util._

import actor.Testing._
import models.database._
import actor.Testing.GetState
import akka.util.Timeout

import scala.reflect._

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
  
  val fail: () => Unit = () => { 
    log.debug(s"Initialization failed.  Becoming idle.")
    context.setReceiveTimeout(Duration.Undefined)
    become(idle)
    unstashAll    
  }
  
  def initializing: Receive = LoggingReceive {
    case Created(response) => {
      response match {
        case Success(request) => {          
          classTag[CrawlRequest].unapply(request).map { r =>
            log.debug(s"Initialization complete.  Becoming processing.")
            become(processing(r, List.empty))
            unstashAll
            context.setReceiveTimeout(10.seconds)
            self ! PageFetchRequest(None, r.id, r.origin, None, None, r.initialRecursionLevel, r.includeExternalLinks)
          }.getOrElse(fail())
        }
        case Failure(ex) => fail()
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
      databaseActor ! UpdateEntities((row: PageFetchRequests) => row.id == request.id, request, PageFetchRequestQuery)
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
    }
    case Updated(Failure(ex)) => {
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


