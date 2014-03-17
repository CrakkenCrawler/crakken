package actors

import scala.util._
import akka.actor._
import akka.event._
import models.database._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.concurrent.Execution.Implicits._

import scalaz.Lens

object DatabaseHelper {
  def create[A <: Entity, B <: Table[A] with TableIdentity](request: A)(implicit mh: ModelHelper[A, B], l: Lens[A, Option[Long]]): Try[A] = DB("crakken") withSession { implicit session =>
    Try {          
      val inputRows = TableQuery(mh.buildTableClass)
      val newId = (inputRows returning inputRows.map(_.id)) += request
      l.set(request, newId)
    }
  }
  def update[A <: Entity, B <: Table[A] with TableIdentity](filter: B => Boolean, transform: A => A)(implicit mh: ModelHelper[A, B]): Try[List[A]] = DB("crakken") withSession { implicit session =>
    Try {
      val inputRows = TableQuery(mh.buildTableClass)
      val transformedRows = inputRows.filter(filter(_)).mapResult(row => transform(row))
      transformedRows.foreach(row => inputRows.where(_.id === row.id.get).update(row))
      transformedRows.iterator.toList
    }
  }
  def delete[A <: Entity, B <: Table[A] with TableIdentity](filter: B => Boolean)(implicit mh: ModelHelper[A, B]): Try[List[A]] = DB("crakken") withSession { implicit session =>
    Try {
      val inputRows = TableQuery(mh.buildTableClass)
      val rowsToDelete = inputRows.filter(filter(_))
      rowsToDelete.delete
      rowsToDelete.iterator.toList
    }
  }
  def get[A <: Entity, B <: Table[A] with TableIdentity](filter: B => Boolean)(implicit mh: ModelHelper[A,B]): Try[List[A]] = DB("crakken") withSession { implicit session =>
    Try {
      val inputRows = TableQuery(mh.buildTableClass)
      inputRows.filter(filter(_)).iterator.toList
    }       
  } 
}

/**
 * Created by 601292 on 3/13/14.
 */
class DatabaseServiceActor extends Actor {
  import DatabaseHelper._
  import ModelHelpers._
  import context._

  def receive = LoggingReceive {
    case CreateCrawlRequest(request) => sender ! CreatedCrawlRequest(create(request))
    case UpdateCrawlRequests(filter, transform) => sender ! UpdatedCrawlRequests(update(filter, transform))
    case DeleteCrawlRequests(filter) => sender ! DeletedCrawlRequests(delete[CrawlRequest, CrawlRequests](filter))
    case GetCrawlRequests(filter) => sender ! GotCrawlRequests(get[CrawlRequest, CrawlRequests](filter))

    case CreatePageFetchRequest(request) => sender ! CreatedPageFetchRequest(create(request))
    case UpdatePageFetchRequests(filter, transform) => sender ! UpdatedPageFetchRequests(update(filter,transform))     
    case DeletePageFetchRequests(filter) =>  sender ! DeletedPageFetchRequests(delete[PageFetchRequest,PageFetchRequests](filter))
    case GetPageFetchRequests(filter) => sender ! GotPageFetchRequests(get[PageFetchRequest,PageFetchRequests](filter))
  }
}


//CrawlRequest inputs
case class CreateCrawlRequest(request: CrawlRequest)
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

