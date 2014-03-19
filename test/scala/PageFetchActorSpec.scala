import actor.{PageFetchSuccess, PageFetchActor}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}

import models.database.PageFetchRequest
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import scala.concurrent._
import org.jsoup.Jsoup
import spray.client.pipelining.SendReceive
import spray.http.{StatusCode, HttpResponse}


object FetchMocker {
  import ExecutionContext.Implicits.global

  def apply(status: Integer, response: String): SendReceive = {
    request => future { HttpResponse(status = StatusCode.int2StatusCode(status),entity = response) }
  }
}
class PageFetchActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("PageFetchActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An PageFetchActor" must {
    "respond with success on status 200" in {
      val content ="<html />"
      val parsedContent = Jsoup.parse(content).html
      val status = 200

      val pageFetchRequestActor = system.actorOf(Props(classOf[PageFetchActor], FetchMocker(status, content)))
      val pfr = PageFetchRequest(Some(1), Some(1), "http://www.google.com", None, None, 1, false)
      pageFetchRequestActor ! pfr
      expectMsg(PageFetchSuccess(pfr.copy(statusCode = Some(status), content = Some(parsedContent))))
    }

    //TODO: Write some negative unit tests
  }
}
