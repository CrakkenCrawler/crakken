package crakken.data.model

import crakken.utils.MongoHelper
import reactivemongo.bson._

case class PageFetchRequest(id: Option[String], crawlRequestId: String, url: String, statusCode: Option[Int], contentId: Option[String], recursionLevel: Int, includeExternalLinks: Boolean)

object PageFetchRequest {
  implicit object PageFetchRequestBSONWriter extends BSONDocumentWriter[PageFetchRequest] {
    def write(request: PageFetchRequest): BSONDocument = {
      BSONDocument(
        "_id" -> MongoHelper.generateOrParse(request.id),
        "crawlRequestId" -> BSONObjectID.parse(request.crawlRequestId).get,
        "statusCode" -> request.statusCode,
        "url" -> request.url,
        "contentId" -> MongoHelper.parseIdIfSome(request.contentId),
        "recursionLevel" -> request.recursionLevel,
        "includeExternalLinks" -> request.includeExternalLinks
      )
    }
  }

  implicit object PageFetchRequestBSONReader extends BSONDocumentReader[PageFetchRequest] {
    def read(doc: BSONDocument): PageFetchRequest = {
      PageFetchRequest(
        id = MongoHelper.stringifyIfSome(doc.getAs[BSONObjectID]("_id")),
        crawlRequestId = doc.getAs[BSONObjectID]("crawlRequestId").get.stringify,
        url = doc.getAs[String]("url").get,
        statusCode = doc.getAs[Int]("statusCode"),
        contentId = MongoHelper.stringifyIfSome(doc.getAs[BSONObjectID]("contentId")),
        recursionLevel = doc.getAs[Int]("recursionLevel").get,
        includeExternalLinks = doc.getAs[Boolean]("includeExternalLinks").get
      )
    }
  }
}