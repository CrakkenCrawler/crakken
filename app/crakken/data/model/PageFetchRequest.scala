package crakken.data.model

import crakken.utils.MongoHelper
import reactivemongo.bson._

case class PageFetchRequest(
                        id: String = BSONObjectID.generate.stringify,
                        crawlRequestId: String,
                        url: String,
                        statusCode: Option[Int],
                        contentId: Option[String],
                        recursionLevel: Int,
                        includeExternalLinks: Boolean)

object PageFetchRequest {
  def apply(
             id: Option[String],
             crawlRequestId: String,
             url: String,
             statusCode: Option[Int],
             contentId: Option[String],
             recursionLevel: Int,
             includeExternalLinks: Boolean)
    = new PageFetchRequest(id.getOrElse(BSONObjectID.generate.stringify), crawlRequestId, url, statusCode, contentId, recursionLevel, includeExternalLinks)


  implicit object PageFetchRequestBSONWriter extends BSONDocumentWriter[PageFetchRequest] {
    def write(request: PageFetchRequest): BSONDocument = {
      BSONDocument(
        "_id" -> BSONObjectID.parse(request.id).get,
        "crawlRequestId" -> BSONObjectID.parse(request.crawlRequestId).get,
        "statusCode" -> request.statusCode,
        "url" -> request.url,
        "contentId" -> MongoHelper.parseIdIfSome(request.contentId)
      )
    }
  }

  implicit object PageFetchRequestBSONReader extends BSONDocumentReader[PageFetchRequest] {
    def read(doc: BSONDocument): PageFetchRequest = {
      PageFetchRequest(
        id = doc.getAs[BSONObjectID]("_id").get.stringify,
        crawlRequestId = doc.getAs[BSONObjectID]("crawlRequestId").get.stringify,
        url = doc.getAs[String]("url").get,
        statusCode = doc.getAs[Int]("statusCode"),
        contentId = MongoHelper.stringifyIfSome(doc.getAs[BSONObjectID]("contentId")),
        recursionLevel = 0,
        includeExternalLinks = false
      )
    }
  }
}