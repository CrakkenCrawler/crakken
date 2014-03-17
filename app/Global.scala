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

  override def onStart(app: Application) {
    val crawlRequestRouter = Akka.system.actorOf(Props[CrawlRequestActor].withRouter(FromConfig()), "crawlRequestRouter")
    val databaseServiceRouter = Akka.system.actorOf(Props[DatabaseServiceActor].withRouter(FromConfig()), "databaseServiceRouter")
  }

  override def onStop(app: Application) {
    val crawlRequestRouter = Akka.system.actorSelection("/user/crawlRequestRouter")
    crawlRequestRouter ! Broadcast(PoisonPill)
    val databaseService = Akka.system.actorSelection("/user/databaseServiceRouter")
    databaseService ! Broadcast(PoisonPill)
  }
}
