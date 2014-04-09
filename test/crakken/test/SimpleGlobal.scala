package crakken.test

import akka.actor._
import crakken.actor._
import crakken.data.repository.MockCrakkenRepository
import play.api.GlobalSettings
import play.libs.Akka._
import spray.client.pipelining._


object SimpleGlobal extends GlobalSettings {
  val actorPrefix = "/user/"

  val repositoryRouterName = "crakkenRepositoryServiceRouter"
  val pageFetchRequestRouterName = "pageFetchRequestRouter"
  val crawlRequestRouterName = "crawlRequestRouter"

  val repositoryRouterPathName = actorPrefix + repositoryRouterName
  val pageFetchRequestRouterPathName = actorPrefix + pageFetchRequestRouterName
  val crawlRequestRouterPathName = actorPrefix + crawlRequestRouterName

  override def onStart(app: play.api.Application) {
    //no routers to eliminate race conditions
    val crakkenRepositoryServiceRouter = system.actorOf(CrakkenRepositoryServiceActor.props(MockCrakkenRepository), repositoryRouterName)
    val pageFetchRequestActor = system.actorOf(PageFetchActor.props(sendReceive(system,system.dispatcher),crakkenRepositoryServiceRouter), pageFetchRequestRouterName)
    system.actorOf(Props(classOf[CrawlRequestActor],pageFetchRequestActor, crakkenRepositoryServiceRouter), crawlRequestRouterName)
  }

  override def onStop(app: play.api.Application) {
    val crawlRequestActor = system.actorSelection(crawlRequestRouterPathName)
    crawlRequestActor ! PoisonPill
    val crakkenRepositoryServiceRouter = system.actorSelection(repositoryRouterPathName)
    crakkenRepositoryServiceRouter ! PoisonPill
    val pageFetchRequestActor = system.actorSelection(pageFetchRequestRouterPathName)
    pageFetchRequestActor ! PoisonPill
    system.shutdown()
  }
}
