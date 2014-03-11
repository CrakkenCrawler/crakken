package actors

import akka.actor._
import akka.event._
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import java.lang.Exception
import scala.concurrent.duration._

import models.database._
import akka.routing._

object CrawlRequestActor {
  def props = Props[CrawlRequestActor]
}

class CrawlRequestActor extends Actor with UnboundedStash with ActorLogging{
  import context._

  val receive = idle


  val pageFetchRouter = context.actorOf(PageFetchActor.props.withRouter(FromConfig()), "pageFetchRouter")

  override def preStart() = {
  }

  override def postStop() = {
    pageFetchRouter ! Broadcast(PoisonPill)
  }
  def idle: Receive = LoggingReceive {
    case (request: CrawlRequest) => {
      DB("crakken") withSession { implicit session =>
        val crawlRequests = TableQuery[CrawlRequests]
        val id = (crawlRequests returning crawlRequests.map(_.id)) += request
        val updatedRequest = CrawlRequest(id, request.origin, request.initialRecursionLevel, request.includeExternalLinks)
        become(processing(updatedRequest, List.empty))
        context.setReceiveTimeout(10.seconds)
        log.debug(s"Request received.  Becoming processing(${updatedRequest}}).")

        self ! PageFetchRequest(None, id, request.origin, None, None, request.initialRecursionLevel, request.includeExternalLinks)
      }
    }
  }

  def processing(crawlRequest: CrawlRequest, history: List[String]): Receive = LoggingReceive {
    case PageFetchSuccess(document, url) => {
      log.debug("Confirmation received.")
    }
    case PageFetchFailure(ex, url) => {
      log.debug("Failure received.")
    }
    case (request: PageFetchRequest) => {
      if (!history.contains(request.url)) {
        become(processing(crawlRequest, history :+ request.url))
        pageFetchRouter ! request
      }
    }

    case ReceiveTimeout => {
      context.setReceiveTimeout(Duration.Undefined)
      become(idle)
      unstashAll
    }
    case _ => stash()
  }
}
