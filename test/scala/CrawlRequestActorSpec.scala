package scala

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import actors._
import models.database._

/**
 * Created by 601292 on 3/6/14.
 */
class CrawlRequestActorSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("CrawlRequestActorSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A CrawlRequestActor" must {
    "begin crawling when told to" in {
      //val crawlRequestActor = system.actorOf(Props[CrawlRequestActor])
      //crawlRequestActor ! CrawlRequest(None, "http://www.google.com", 1, false)
    }
  }

}
