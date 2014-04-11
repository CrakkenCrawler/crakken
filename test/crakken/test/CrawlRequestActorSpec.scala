package crakken.test

import akka.actor._
import akka.testkit._
import crakken.actor.Testing._
import crakken.actor._
import crakken.data.model.{PageFetchRequest, CrawlRequest}
import crakken.data.repository.{PageFetchRequestMessages, CrawlRequestMessages}
import org.scalatest._
import scala.concurrent.duration._
import scala.util.Success

class CrawlRequestActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("PageFetchActorSpec"))

  val databaseProbe = TestProbe()
  val pageFetchRequestProbe = TestProbe()
  val crawlRequestActor = system.actorOf(Props(classOf[CrawlRequestActor],pageFetchRequestProbe.ref, databaseProbe.ref), "crawlRequestActor")

  val deathwatchProbe = TestProbe()
  deathwatchProbe watch crawlRequestActor


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A CrawlRequestActor on happy path" must {
    val cr = CrawlRequest(None, "http://www.google.com", 1, false)
    val crWithId = cr.copy(id = Some("1"))
    val pfr = PageFetchRequest(None, crWithId.id.get, crWithId.origin, None, None, crWithId.initialRecursionLevel, crWithId.includeExternalLinks)
    val pfrWithId = pfr.copy(id = Some("1"))
    val pfrWithIdAndContent = pfrWithId.copy(contentId = Some("<html />"), statusCode = Some(200))

    "start out in an idle state" in {
      crawlRequestActor ! GetState()
      expectMsg(Idle())
    }
    "request that the DB insert a new crawl request in the database and become(initializing) when a new request is received" in {
      crawlRequestActor ! cr
      databaseProbe.expectMsg(CrawlRequestMessages.create(cr))
      crawlRequestActor ! GetState()
      expectMsg(Initializing())
    }
    "on successful initialization request, insert a new PFR into the database for the origin" in {
      crawlRequestActor ! CrawlRequestMessages.created(Success(crWithId))
      databaseProbe.expectMsg(PageFetchRequestMessages.create(pfr))
    }
    "on successful creation of the origin PFR row, become(processing) and ask the PageFetchActor to fetch" in {
      crawlRequestActor ! PageFetchRequestMessages.created(Success(pfrWithId))
      pageFetchRequestProbe.expectMsg(pfrWithId)
      crawlRequestActor ! GetState()
      expectMsg(Processing())
    }
    "on successful fetch, ask the database to update the result" in {
      crawlRequestActor ! PageFetchSuccess(pfrWithIdAndContent)
      databaseProbe.expectMsgClass(classOf[PageFetchRequestMessages.update])
      crawlRequestActor ! GetState()
      expectMsg(Processing())
    }
    "not crawl the same page twice" in {
      crawlRequestActor ! pfr
      databaseProbe.expectNoMsg(3.seconds)
      pageFetchRequestProbe.expectNoMsg(3.seconds)
    }
    "terminate gracefully when asked" in {
      crawlRequestActor ! PoisonPill
      deathwatchProbe.expectTerminated(crawlRequestActor)
    }

    //TODO write some negative unit tests
  }

}

