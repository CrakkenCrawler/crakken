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
import scala.util.Success
import crakken.data.model.PageFetchRequest
import crakken.data.model.CrawlRequest
import crakken.actor.Testing.Initializing
import scala.util.Failure

class CrawlRequestActor(val pageFetchActor: ActorRef, val repositoryActor: ActorRef) extends Actor with UnboundedStash with ActorLogging {
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
      repositoryActor ! CrawlRequestMessages.create(request)
    }

    case GetState() => sender ! Idle()
  }
  
  def initializing: Receive = LoggingReceive {
    case CrawlRequestMessages.created(response) => {
      response match {
        case Success(cr) => {
            log.debug(s"Initialization complete.  Becoming processing. ${cr}")
            become(processing(cr, List.empty))
            unstashAll
            context.setReceiveTimeout(10.seconds)
            self ! PageFetchRequest(None, cr.id.get, cr.origin, None, None, cr.initialRecursionLevel, cr.includeExternalLinks)
        }
        case Failure(ex) => {
          log.debug(s"Initialization failed.  Becoming idle.")
          context.setReceiveTimeout(Duration.Undefined)
          become(idle)
          unstashAll
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

    //PageFetchRequest Messages
    case PageFetchSuccess(request) => {
      repositoryActor ! PageFetchRequestMessages.update(request)
    }
    case PageFetchFailure(request, ex) => {
      log.debug("Page fetch failure received.")
    }
    case (request: PageFetchRequest) => {
      implicit val timeout = Timeout(5.seconds)
      log.debug("Child page fetch request received.")
      if (!history.contains(request.url)) {
        become(processing(crawlRequest, history :+ request.url))
        repositoryActor ! PageFetchRequestMessages.create(request)
      }
    }

    //Repository Responses
    case PageFetchRequestMessages.created(Success(pageFetchRequest)) => {
      pageFetchActor ! pageFetchRequest
    }
    case PageFetchRequestMessages.created(Failure(ex)) => {
      log.error(ex,"PageFetchRequestMessages.create failed" )
    }

    case PageFetchRequestMessages.updated(Success(pageFetchRequests)) => {

    }
    case PageFetchRequestMessages.updated(Failure(ex)) => {
      log.error(ex,"PageFetchRequestMessages.update failed" )
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


