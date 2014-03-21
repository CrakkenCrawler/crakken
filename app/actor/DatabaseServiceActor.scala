package actor

import scala.util._
import akka.actor._
import akka.event._
import models.database._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

import scala.slick.lifted.CanBeQueryCondition

//trait DatabaseActor extends Actor with ActorLogging {
//
//  def update[T <: Table[_]](f: CanBeQueryCondition[T], newRow: T#TableElementType): Try[Int] = DB withSession { implicit session =>
//    Try {
//      val inputRows = TableQuery[T]
//      val rowsUpdated = inputRows.filter(f).update(newRow)
//      //val transformedRows = filteredRows.map(row => transform(row))
//      log.debug(s"Update received. ${rowsUpdated} rows updated.")
//      rowsUpdated
//    }
//  }
//  def delete[T <: Table[_]](f: CanBeQueryCondition[T]): Try[Int] = DB withSession { implicit session =>
//    Try {
//      val inputRows = TableQuery[T]
//      val rowsDeleted = inputRows.where(f).delete
//      log.debug(s"Delete received. ${rowsDeleted} rows deleted.")
//      rowsDeleted
//    }
//  }
//  def get[T <: Table[_]](f: CanBeQueryCondition[T]): Try[List[T#TableElementType]] = DB withSession { implicit session =>
//    Try {
//      val inputRows = TableQuery[T]
//      val rowsSelected = inputRows.filter(f).run.toList
//      log.debug(s"Get received. ${rowsSelected.length} rows returned.")
//      rowsSelected
//    }       
//  } 
//}

class DatabaseServiceActor extends Actor with ActorLogging {

  def receive = LoggingReceive {
    //case CreateCrawlRequest(request) => sender ! CreatedCrawlRequest(create[CrawlRequests](request))
    case CreateEntity(e, q) => sender ! Created(DB withSession { implicit session =>
      Try {          
        (q returning q) += e
      }
    })
    case UpdateEntities(f, newRow, q) => sender ! Updated(DB withSession { implicit session =>
      Try {
        val rowsUpdated = q.filter(f).update(newRow)
        //val transformedRows = filteredRows.map(row => transform(row))
        log.debug(s"Update received. ${rowsUpdated} rows updated.")
        rowsUpdated
      }
    })
    case DeleteEntities(f, q) => sender ! Deleted(DB withSession { implicit session =>
      Try {
        val rowsDeleted = q.filter(f).delete
        log.debug(s"Delete received. ${rowsDeleted} rows deleted.")
        rowsDeleted
      }
    })
    case GetEntities(f, q) => sender ! Entities(DB withSession { implicit session =>
      Try {
        val rowsSelected = q.filter(f).run.toList
        log.debug(s"Get received. ${rowsSelected.length} rows returned.")
        rowsSelected
      }       
    })
  }
}


//CrawlRequest inputs
//case class CreateCrawlRequest(request: CrawlRequest)
//case class UpdateCrawlRequests(filter: CanBeQueryCondition[CrawlRequests], transform: CrawlRequest)
//case class DeleteCrawlRequests(filter: CanBeQueryCondition[CrawlRequests])
//case class GetCrawlRequests(filter: CanBeQueryCondition[CrawlRequests])

//CrawlRequest outputs
//case class CreatedCrawlRequest(response: Try[CrawlRequest])
//case class UpdatedCrawlRequests(response: Try[Int])
//case class DeletedCrawlRequests(response: Try[Int])
//case class GotCrawlRequests(response: Try[List[CrawlRequest]])

//PageFetchRequest inputs
//case class CreatePageFetchRequest(request: PageFetchRequest)
//case class UpdatePageFetchRequests(filter: CanBeQueryCondition[PageFetchRequests], transform: PageFetchRequest)
//case class DeletePageFetchRequests(filter: CanBeQueryCondition[PageFetchRequests])
//case class GetPageFetchRequests(filter: CanBeQueryCondition[PageFetchRequests])

//PageFetchRequest outputs
//case class CreatedPageFetchRequest(response: Try[PageFetchRequest])
//case class UpdatedPageFetchRequests(response: Try[Int])
//case class DeletedPageFetchRequests(response: Try[Int])
//case class GotPageFetchRequests(response: Try[List[PageFetchRequest]])

