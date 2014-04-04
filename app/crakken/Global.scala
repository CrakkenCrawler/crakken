package crakken

import _root_.crakken.data.repository.MongoCrakkenRepository
import _root_.crakken.actor._
import akka.actor._
import akka.routing._
import play.api._
import play.libs.Akka._
import spray.client.pipelining._


object Global extends GlobalSettings{
  val actorPrefix = "/user/"

  val repositoryRouterName = "crakkenRepositoryServiceRouter"
  val pageFetchRequestRouterName = "pageFetchRequestRouter"
  val crawlRequestRouterName = "crawlRequestRouter"

  val repositoryRouterPathName = actorPrefix + repositoryRouterName
  val pageFetchRequestRouterPathName = actorPrefix + pageFetchRequestRouterName
  val crawlRequestRouterPathName = actorPrefix + crawlRequestRouterName


  override def onStart(app: Application) {
    val repository = MongoCrakkenRepository
    val crakkenRepositoryServiceRouter = system.actorOf(CrakkenRepositoryServiceActor.props(repository).withRouter((FromConfig())), repositoryRouterName)
    val pageFetchRequestRouter = system.actorOf(Props(classOf[PageFetchActor],sendReceive(system,system.dispatcher)).withRouter(FromConfig()), pageFetchRequestRouterName)
    system.actorOf(Props(classOf[CrawlRequestActor],pageFetchRequestRouter, crakkenRepositoryServiceRouter).withRouter(FromConfig()), crawlRequestRouterName)
  }

  override def onStop(app: Application) {
    val crawlRequestRouter = system.actorSelection(crawlRequestRouterPathName)
    crawlRequestRouter ! Broadcast(PoisonPill)
    val pageFetchRequestRouter = system.actorSelection(pageFetchRequestRouterPathName)
    pageFetchRequestRouter ! Broadcast(PoisonPill)
    val crakkenRepositoryServiceRouter = system.actorSelection(repositoryRouterPathName)
    crakkenRepositoryServiceRouter ! Broadcast(PoisonPill)
    system.shutdown()
  }
}
