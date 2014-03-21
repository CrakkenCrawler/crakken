package models.database

import play.api.db.slick.Config.driver.simple._

case class CrawlRequest(id: Option[Long], origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean)

class CrawlRequests(tag: Tag) extends Table[CrawlRequest](tag, "CRAWL_REQUESTS") {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def origin = column[String]("ORIGIN", O.DBType("VARCHAR(2048)"))
  def initialRecursionLevel = column[Int]("INITIAL_RECURSION_LEVEL")
  def includeExternalLinks = column[Boolean]("INCLUDE_EXTERNALS")
  def * = (id, origin, initialRecursionLevel, includeExternalLinks) <> (CrawlRequest.tupled, CrawlRequest.unapply )
}

object CrawlRequestQuery extends TableQuery(new CrawlRequests(_))