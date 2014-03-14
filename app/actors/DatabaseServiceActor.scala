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
          val inputRows = TableQuery[CrawlRequests]
          val newId = (inputRows returning inputRows.map(_.id)) += request
          request.copy(id = newId)
        }
        sender ! CreatedCrawlRequest(response)
      }
    }
    case UpdateCrawlRequests(filter, transform) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[CrawlRequests]
          val transformedRows = inputRows.filter(filter(_)).mapResult(row => transform(row))
          transformedRows.foreach(row => inputRows.where(_.id === row.id.get).update(row))
          transformedRows.iterator.toList
        }
        sender ! UpdatedCrawlRequests(response)
      }
    }
    case DeleteCrawlRequests(filter) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[CrawlRequests]
          val rowsToDelete = inputRows.filter(filter(_))
          rowsToDelete.delete
          rowsToDelete.iterator.toList
        }
        sender ! DeletedCrawlRequests(response)
      }
    }
    case GetCrawlRequests(filter) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[CrawlRequests]
          inputRows.filter(filter(_)).iterator.toList
        }
        sender ! GotCrawlRequests(response)
      }
    }

    //
    case CreatePageFetchRequest(request) => {
      DB("crakken") withSession { implicit session =>
        val response = Try[PageFetchRequest] {
          val inputRows = TableQuery[PageFetchRequests]
          val newId = (inputRows returning inputRows.map(_.id)) += request
          request.copy(id = newId)
        }
        sender ! CreatedPageFetchRequest(response)
      }
    }
    case UpdatePageFetchRequests(filter, transform) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[PageFetchRequests]
          val transformedRows = inputRows.filter(filter(_)).mapResult(row => transform(row))
          transformedRows.foreach(row => inputRows.where(_.id === row.id.get).update(row))
          transformedRows.iterator.toList
        }
        sender ! UpdatedPageFetchRequests(response)
      }
    }
    case DeletePageFetchRequests(filter) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[PageFetchRequests]
          val rowsToDelete = inputRows.filter(filter(_))
          rowsToDelete.delete
          rowsToDelete.iterator.toList
        }
        sender ! DeletedPageFetchRequests(response)
      }
    }
    case GetPageFetchRequests(filter) => {
      DB("crakken") withSession { implicit session =>
        val response = Try {
          val inputRows = TableQuery[PageFetchRequests]
          inputRows.filter(filter(_)).iterator.toList
        }
        sender ! GotPageFetchRequests(response)
      }
    }
  }}


//CrawlRequest inputs
case class Create[T](request: T)
case class UpdateCrawlRequests(filter: CrawlRequests => Boolean, transform: CrawlRequest => CrawlRequest)
case class DeleteCrawlRequests(filter: CrawlRequests => Boolean)
case class GetCrawlRequests(filter: CrawlRequests => Boolean)

//CrawlRequest outputs
case class CreatedCrawlRequest(response: Try[CrawlRequest])
case class UpdatedCrawlRequests(response: Try[List[CrawlRequest]])
case class DeletedCrawlRequests(response: Try[List[CrawlRequest]])
case class GotCrawlRequests(response: Try[List[CrawlRequest]])

//PageFetchRequest inputs
case class CreatePageFetchRequest(request: PageFetchRequest)
case class UpdatePageFetchRequests(filter: PageFetchRequests => Boolean, transform: PageFetchRequest => PageFetchRequest)
case class DeletePageFetchRequests(filter: PageFetchRequests => Boolean)
case class GetPageFetchRequests(filter: PageFetchRequests => Boolean)

//PageFetchRequest outputs
case class CreatedPageFetchRequest(response: Try[PageFetchRequest])
case class UpdatedPageFetchRequests(response: Try[List[PageFetchRequest]])
case class DeletedPageFetchRequests(response: Try[List[PageFetchRequest]])
case class GotPageFetchRequests(response: Try[List[PageFetchRequest]])

