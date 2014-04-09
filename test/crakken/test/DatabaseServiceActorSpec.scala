package crakken.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import crakken.actor._
import crakken.data.model.CrawlRequest
import crakken.data.repository._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import play.api.test.FakeApplication
import play.api.test.Helpers._
import scala.util.Success

class DatabaseServiceActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("DatabaseServiceActorSpec"))
  val settings = inMemoryDatabase()

  "A DatabaseServiceActor" must {

    "create and update a new CR" in {

      running(FakeApplication()) {
        val repositoryActor = system.actorOf(CrakkenRepositoryServiceActor.props(MockCrakkenRepository))

        val cr = CrawlRequest(None, "http://www.google.com", 1, false)
        val crWithId = cr.copy(id = Some("1"))
        //val pfr = PageFetchRequest(None, crWithId.id.get, crWithId.origin, None, None, crWithId.initialRecursionLevel, crWithId.includeExternalLinks)
        //val pfrWithId = pfr.copy(id = Some("1"))
        //val pfrWithIdAndContent = pfr.copy(content = Some("<html />"), statusCode = Some(200))

        repositoryActor ! CrawlRequestMessages.create(cr)
        expectMsg(CrawlRequestMessages.created(Success(cr.copy(id = Some("123abc")))))
      }
    }
  }


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
