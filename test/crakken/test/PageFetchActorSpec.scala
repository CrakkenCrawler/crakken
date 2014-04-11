package crakken.test

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import crakken.actor.{PageFetchSuccess, PageFetchActor}
import crakken.data.model.PageFetchRequest
import org.jsoup.Jsoup
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

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
      val pfr = PageFetchRequest(Some("1"), "1", "http://www.google.com", None, None, 1, false)
      pageFetchRequestActor ! pfr
      expectMsg(PageFetchSuccess(pfr.copy(statusCode = Some(status), contentId = Some(parsedContent))))
    }

    //TODO: Write some negative unit tests
  }
}
