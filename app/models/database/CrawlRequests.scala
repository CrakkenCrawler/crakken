package models.database

import play.api.db.slick.Config.driver.simple._

case class CrawlRequest(id: Option[Long], origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean) extends Entity

class CrawlRequests(tag: Tag) extends Table[CrawlRequest](tag, "CRAWL_REQUESTS") with TableIdentity {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def origin = column[String]("ORIGIN", O.DBType("VARCHAR(2048)"))
  def initialRecursionLevel = column[Int]("INITIAL_RECURSION_LEVEL")
  def includeExternalLinks = column[Boolean]("INCLUDE_EXTERNALS")
  def * = (id, origin, initialRecursionLevel, includeExternalLinks) <> (CrawlRequest.tupled, CrawlRequest.unapply )
}
