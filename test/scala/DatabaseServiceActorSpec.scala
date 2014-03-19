package scala

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import play.api.test._
import play.api.test.Helpers._
import play.libs.Akka
import actor.{CreateCrawlRequest, DatabaseServiceActor}
import models.database.{PageFetchRequest, CrawlRequest}

class DatabaseServiceActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  /*
  val app = new FakeApplication(additionalConfiguration = inMemoryDatabase())
  running(fakeApp = app) {

    "A DatabaseServiceActor" must {
      val databaseActor = Akka.system.actorOf(Props(classOf[DatabaseServiceActor]))

      val cr = CrawlRequest(None, "http://www.google.com", 1, false)
      val crWithId = cr.copy(id = Some(1))
      val pfr = PageFetchRequest(None, crWithId.id, crWithId.origin, None, None, crWithId.initialRecursionLevel, crWithId.includeExternalLinks)
      val pfrWithId = pfr.copy(id = Some(1))
      val pfrWithIdAndContent = pfrWithId.copy(content = Some("<html />"), statusCode = Some(200))

      "create a new CR" in {
        databaseActor ! CreateCrawlRequest(cr)
        expectMsg(crWithId)
      }
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  */
}
