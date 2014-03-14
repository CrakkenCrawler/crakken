package actors

import akka.actor._
import akka.event._
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import java.lang.Exception
import scala.concurrent.duration._
import scala.util._

import models.database._
import akka.routing._

object CrawlRequestActor {
  def props = Props[CrawlRequestActor]
}

class CrawlRequestActor extends Actor with UnboundedStash with ActorLogging {
  import context._

  val receive = idle

  val pageFetchRouter = context.actorOf(PageFetchActor.props.withRouter(FromConfig()), "pageFetchRouter")
  val databaseServiceRouter = context.actorSelection("/user/databaseServiceRouter")

  override def preStart() = {
  }

  override def postStop() = {
    pageFetchRouter ! Broadcast(PoisonPill)
    databaseServiceRouter ! Broadcast(PoisonPill)
  }

  def idle: Receive = LoggingReceive {
    case (request: CrawlRequest) => {
      log.debug(s"Request received.  Becoming initializing.")
      context.setReceiveTimeout(10.seconds)
      become(initializing)
      databaseServiceRouter ! CreateCrawlRequest(request)
    }
  }

  def initializing: Receive = LoggingReceive {
    case CreatedCrawlRequest(response) => {
      response match {
        case Success(request) => {
          log.debug(s"Initialization complete.  Becoming processing.")
          become(processing(request, List.empty))
          context.setReceiveTimeout(10.seconds)
          self ! PageFetchRequest(None, request.id, request.origin, None, None, request.initialRecursionLevel, request.includeExternalLinks)
        }
        case Failure(ex) => {
          log.debug(s"Initialization failed.  Becoming idle.")
          context.setReceiveTimeout(Duration.Undefined)
          become(idle)
        }
      }
    }
    case ReceiveTimeout => {
      log.debug(s"Initialization timed out.  Becoming idle.")
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
    }

    case _ => stash
  }

  def processing(crawlRequest: CrawlRequest, history: List[String]): Receive = LoggingReceive {
    case PageFetchSuccess(document, url) => {
      log.debug("Page fetch received.")
    }
    case PageFetchFailure(ex, url) => {
      log.debug("Page fetch failure received.")
    }
    case (request: PageFetchRequest) => {
      log.debug("Child page fetch request received.")
      if (!history.contains(request.url)) {
        become(processing(crawlRequest, history :+ request.url))
        pageFetchRouter ! request
      }
    }

    case ReceiveTimeout => {
      log.debug("No activity received for 10 seconds.  Becoming idle.")
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
      unstashAll
    }
    case _ => stash()
  }
}
