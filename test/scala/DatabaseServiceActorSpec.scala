package scala

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import play.api.test.Helpers._
import actor._
import actor.CreateCrawlRequest
import scala.util.Success
import models.database.PageFetchRequest
import models.database.CrawlRequest
import play.api.test.FakeApplication

class DatabaseServiceActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("DatabaseServiceActorSpec"))

  "A DatabaseServiceActor" must {

    "create a new CR" in {
      running(FakeApplication()) {
        val databaseActor = system.actorOf(Props(classOf[DatabaseServiceActor]))

        val cr = CrawlRequest(None, "http://www.google.com", 1, false)
        val crWithId = cr.copy(id = Some(1))
        val pfr = PageFetchRequest(None, crWithId.id, crWithId.origin, None, None, crWithId.initialRecursionLevel, crWithId.includeExternalLinks)
        val pfrWithId = pfr.copy(id = Some(1))
        val pfrWithIdAndContent = pfrWithId.copy(content = Some("<html />"), statusCode = Some(200))

        databaseActor ! CreatePageFetchRequest(pfr)
        expectMsg(CreatedPageFetchRequest(Success(pfrWithId)))
        databaseActor ! UpdatePageFetchRequests(row => row.id === pfrWithId.id, row => pfrWithIdAndContent)
        expectMsg(UpdatedPageFetchRequests(Success(List(pfrWithIdAndContent))))
      }
    }
  }


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
