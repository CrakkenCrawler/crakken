package scala

import actor._
import akka.actor._
import play.api.GlobalSettings
import spray.client.pipelining._

import play.libs.Akka._

object SimpleGlobal extends GlobalSettings {

  override def onStart(app: play.api.Application) {
    val databaseServiceActor = system.actorOf(Props[DatabaseServiceActor], "databaseServiceActor")
    val pageFetchRequestActor = system.actorOf(Props(classOf[PageFetchActor],sendReceive(system,system.dispatcher)), "pageFetchRequestActor")
    system.actorOf(Props(classOf[CrawlRequestActor],pageFetchRequestActor, databaseServiceActor), "crawlRequestActor")
  }

  override def onStop(app: play.api.Application) {
    val crawlRequestActor = system.actorSelection("/user/crawlRequestActor")
    crawlRequestActor ! PoisonPill
    val databaseService = system.actorSelection("/user/databaseServiceActor")
    databaseService ! PoisonPill
    val pageFetchRequestActor = system.actorSelection("/user/pageFetchRequestActor")
    pageFetchRequestActor ! PoisonPill
    system.shutdown()
  }
}
