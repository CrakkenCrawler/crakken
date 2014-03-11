import akka.actor._
import akka.routing._
import models.database.CrawlRequests
import play.api.db._
import play.api._
import play.api.GlobalSettings
import play.api.db.slick.Config.driver.simple._
import com.typesafe.config._
import actors._

import play.api.Play.current
import play.libs.Akka

object Global extends GlobalSettings{
  //val log = Logging(Akka.system, this)

  override def onStart(app: Application) {
    val crawlRequestRouter = Akka.system.actorOf(Props[CrawlRequestActor].withRouter(FromConfig()), "crawlRequestRouter")
  }

  override def onStop(app: Application) {
    val crawlRequestRouter = Akka.system.actorSelection("/crawlRequestRouter")
    crawlRequestRouter ! Broadcast(PoisonPill)
  }
}
