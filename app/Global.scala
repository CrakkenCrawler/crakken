import actor._
import akka.actor._
import akka.routing._
import akka.routing.Broadcast
import play.api._
import play.api.GlobalSettings
import spray.client.pipelining._

import play.libs.Akka._

object Global extends GlobalSettings{

  override def onStart(app: Application) {
    val databaseServiceRouter = system.actorOf(Props[DatabaseServiceActor].withRouter(FromConfig()), "databaseServiceRouter")
    val pageFetchRequestRouter = system.actorOf(Props(classOf[PageFetchActor],sendReceive(system,system.dispatcher)).withRouter(FromConfig()), "pageFetchRequestRouter")
    system.actorOf(Props(classOf[CrawlRequestActor],pageFetchRequestRouter, databaseServiceRouter).withRouter(FromConfig()), "crawlRequestRouter")
  }

  override def onStop(app: Application) {
    val crawlRequestRouter = system.actorSelection("/user/crawlRequestRouter")
    crawlRequestRouter ! Broadcast(PoisonPill)
    val databaseService = system.actorSelection("/user/databaseServiceRouter")
    databaseService ! Broadcast(PoisonPill)
    val pageFetchRequestRouter = system.actorSelection("/user/pageFetchRequestRouter")
    pageFetchRequestRouter ! Broadcast(PoisonPill)
    system.shutdown()
  }
}
