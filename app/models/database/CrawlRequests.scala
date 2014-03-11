package models.database

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.slick.Config.driver.simple._
import play.api.data.format.Formats._
import slick.jdbc._
import utils._

case class CrawlRequest(id: Option[Long], origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean)

class CrawlRequests(tag: Tag) extends Table[CrawlRequest](tag, "CRAWL_REQUESTS") {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def origin = column[String]("ORIGIN")
  def initialRecursionLevel = column[Int]("INITIAL_RECURSION_LEVEL")
  def includeExternalLinks = column[Boolean]("INCLUDE_EXTERNALS")
  def * = (id, origin, initialRecursionLevel, includeExternalLinks) <> (CrawlRequest.tupled, CrawlRequest.unapply )
}
