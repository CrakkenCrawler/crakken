package models.database

import play.api.db.slick.Config.driver.simple._

case class PageFetchRequest(id: Option[Long],
                            crawlRequestId: Option[Long],
                            url: String,
                            statusCode: Option[Int],
                            content: Option[String],
                            recursionLevel: Int,
                            includeExternalLinks: Boolean)

class PageFetchRequests(tag: Tag) extends Table[PageFetchRequest](tag, "PAGE_FETCH_REQUESTS") {
  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  def crawlRequestId = column[Option[Long]]("CRAWLREQUEST_ID")
  def url = column[String]("URL", O.DBType("VARCHAR(2048)"))
  def statusCode = column[Option[Int]]("STATUS_CODE")
  def content = column[Option[String]]("CONTENT", O.DBType("LONGTEXT"))
  def recursionLevel = column[Int]("RECURSION_LEVEL")
  def includeExternalLinks = column[Boolean]("INCLUDE_EXTERNALS")
  def * = (id, crawlRequestId, url, statusCode, content, recursionLevel, includeExternalLinks) <> (PageFetchRequest.tupled, PageFetchRequest.unapply)
}

object PageFetchRequestQuery extends TableQuery(new PageFetchRequests(_))