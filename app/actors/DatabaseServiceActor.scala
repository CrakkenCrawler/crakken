package actors

import akka.actor._
import akka.event._
import scala.util._
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current


import models.database._

/**
 * Created by 601292 on 3/13/14.
 */
class DatabaseServiceActor extends Actor {
  import context._

  def receive = LoggingReceive {
    case CreateCrawlRequest(request) => {
      DB("crakken") withSession { implicit session =>
        val response = Try[CrawlRequest] {
          val crawlRequests = TableQuery[CrawlRequests]
          val id = (crawlRequests returning crawlRequests.map(_.id)) += request
          CrawlRequest(id, request.origin, request.initialRecursionLevel, request.includeExternalLinks)
        }
        sender ! CreatedCrawlRequest(response)
      }
    }
    case UpdateCrawlRequests(filter, transform) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val crawlRequests = TableQuery[CrawlRequests]
          val transformedRows = crawlRequests.filter(filter(_)).mapResult(row => transform(row))
          transformedRows.foreach(row => crawlRequests.where(_.id === row.id.get).update(row))
          transformedRows.iterator.toList
        }
        sender ! UpdatedCrawlRequests(response)
      }
    }
    case DeleteCrawlRequests(filter) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val crawlRequests = TableQuery[CrawlRequests]
          val rowsToDelete = crawlRequests.filter(filter(_))
          rowsToDelete.delete
          rowsToDelete.iterator.toList
        }
        sender ! DeletedCrawlRequests(response)
      }
    }
  }
}

//CrawlRequest inputs
case class CreateCrawlRequest(request: CrawlRequest)
case class UpdateCrawlRequests(filter: CrawlRequests => Boolean, transform: CrawlRequest => CrawlRequest)
case class DeleteCrawlRequests(filter: CrawlRequests => Boolean)
case class GetCrawlRequests(filter: CrawlRequest => Boolean)

//CrawlRequest outputs
case class CreatedCrawlRequest(response: Try[CrawlRequest])
case class UpdatedCrawlRequests(response: Try[List[CrawlRequest]])
case class DeletedCrawlRequests(response: Try[List[CrawlRequest]])
case class GotCrawlRequests(response: Try[List[CrawlRequest]])

//PageFetchRequest inputs
case class CreatePageFetchRequest(request: PageFetchRequest)
case class UpdatePageFetchRequests(filter: PageFetchRequest => Boolean, transform: PageFetchRequest => PageFetchRequest)
case class DeletePageFetchRequests(filter: PageFetchRequest => Boolean)
case class GetPageFetchRequests(filter: PageFetchRequest => Boolean)

//PageFetchRequest outputs
case class CreatedPageFetchRequest(response: Try[PageFetchRequest])
case class UpdatedPageFetchRequests(response: Try[List[PageFetchRequest]])
case class DeletedPageFetchRequests(response: Try[Integer])
case class GotPageFetchRequests(response: Try[List[PageFetchRequest]])

