import actors._
import akka.actor.{PoisonPill, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.event.Logging
import models.database.PageFetchRequest
import org.jsoup.nodes.Document
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import spray.http.{StatusCodes, HttpResponse}

class PageFetchActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("PageFetchActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An PageFetchActor" must {
    /*"return content from eyemed.com" in {
      val actorRef = system.actorOf(PageFetchActor.props)
      actorRef ! PageFetchRequest(None, None, "http://portal.eyemedvisioncare.com/wps/portal/em/eyemed", None, 0, false)
      val response = expectMsgClass(classOf[PageFetchSuccess])
      //println(response.html)
    }
    "find and return a DOM Document for https://www.google.com over HTTPS" in {
      val actorRef = system.actorOf(PageFetchActor.props)
      actorRef ! PageFetchRequest(None, None, "https://www.google.com", None, 0, false)
      val response = expectMsgClass(classOf[PageFetchSuccess])
      //println(response.html)
    }
    "find and return a document over HTTPS with a bad certificate" in {
      val actorRef = system.actorOf(PageFetchActor.props)
      actorRef ! PageFetchRequest(None, None, "https://atl-emuat1.lenscrafters.com/locator/locator.emvc", None, 0, false)
      val response = expectMsgClass(classOf[PageFetchSuccess])
      //println(response.html)
    }
    "fail with a 404 status when looking at http://www.google.com/nosuchurl" in {
      val actorRef = system.actorOf(PageFetchActor.props)
      actorRef ! PageFetchRequest(None, None, "http://www.google.com/nosuchurl", None, 0, false)
      val response = expectMsgClass(classOf[PageFetchSuccess])
      //println(response.html)
    }  */
  }
}
