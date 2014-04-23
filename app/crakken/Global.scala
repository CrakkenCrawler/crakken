package crakken

import _root_.crakken.data.repository.MongoCrakkenRepository
import _root_.crakken.actor._
import akka.actor._
import akka.routing._
import play.api._
import play.libs.Akka._
import spray.client.pipelining._
import play.modules.reactivemongo.ReactiveMongoPlugin

object Global extends GlobalSettings{
  val actorPrefix = "/user/"

  val statusUpdateRouterName = "statusUpdateRouter"
  val repositoryRouterName = "crakkenRepositoryServiceRouter"
  val pageFetchRequestRouterName = "pageFetchRequestRouter"
  val crawlRequestRouterName = "crawlRequestRouter"

  val statusUpdateRouterPathName = actorPrefix + statusUpdateRouterName
  val repositoryRouterPathName = actorPrefix + repositoryRouterName
  val pageFetchRequestRouterPathName = actorPrefix + pageFetchRequestRouterName
  val crawlRequestRouterPathName = actorPrefix + crawlRequestRouterName


  override def onStart(app: Application) {
    val repository = MongoCrakkenRepository
    val statusUpdateRouter = system.actorOf(StatusActor.props.withRouter(FromConfig), statusUpdateRouterName)
    val crakkenRepositoryServiceRouter = system.actorOf(CrakkenRepositoryServiceActor.props(repository).withRouter(FromConfig), repositoryRouterName)
    val pageFetchRequestRouter = system.actorOf(PageFetchActor.props(sendReceive(system,system.dispatcher), crakkenRepositoryServiceRouter).withRouter(FromConfig), pageFetchRequestRouterName)
    val crawlRequestRouter = system.actorOf(CrawlRequestActor.props(pageFetchRequestRouter, crakkenRepositoryServiceRouter, statusUpdateRouter).withRouter(FromConfig), crawlRequestRouterName)
  }

  override def onStop(app: Application) {
    val statusUpdateRouter = system.actorSelection(statusUpdateRouterPathName)
    statusUpdateRouter ! Broadcast(PoisonPill)
    val crawlRequestRouter = system.actorSelection(crawlRequestRouterPathName)
    crawlRequestRouter ! Broadcast(PoisonPill)
    val pageFetchRequestRouter = system.actorSelection(pageFetchRequestRouterPathName)
    pageFetchRequestRouter ! Broadcast(PoisonPill)
    val crakkenRepositoryServiceRouter = system.actorSelection(repositoryRouterPathName)
    crakkenRepositoryServiceRouter ! Broadcast(PoisonPill)
    system.shutdown()
  }
}
