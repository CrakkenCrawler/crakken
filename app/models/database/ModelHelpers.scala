package models.database

import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.db.slick.Config.driver.simple._

import scalaz.Lens

trait Entity {
  def id: Option[Long] 
}

trait TableIdentity {
  def id: Column[Option[Long]]
}

trait ModelHelper[A <: Entity, B <: Table[A] with TableIdentity] {
  def buildTableClass(tag: Tag): B
}

object ModelHelpers {
  implicit object CrawlRequestsHelper extends ModelHelper[CrawlRequest, CrawlRequests] {
    def buildTableClass(tag: Tag): CrawlRequests = new CrawlRequests(tag)  
  }

  implicit object PageFetchRequestsHelper extends ModelHelper[PageFetchRequest, PageFetchRequests] {
    def buildTableClass(tag: Tag): PageFetchRequests = new PageFetchRequests(tag)  
  }

  implicit val cr_id = Lens.lensu[CrawlRequest,Option[Long]]((cr, i) => cr.copy(id = i), _.id)
  implicit val pf_id = Lens.lensu[PageFetchRequest, Option[Long]]((pf, i) => pf.copy(id = i), _.id)
}