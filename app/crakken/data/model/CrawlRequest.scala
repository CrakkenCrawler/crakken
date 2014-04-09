package crakken.data.model

import crakken.utils.MongoHelper
import reactivemongo.bson._

case class CrawlRequest(id: Option[String], origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean )

object CrawlRequest {
  implicit object CrawlRequestBSONWriter extends BSONDocumentWriter[CrawlRequest] {
    def write(request: CrawlRequest): BSONDocument = {
      BSONDocument(
        "_id" -> MongoHelper.generateOrParse(request.id),
        "origin" -> request.origin,
        "initialRecursionLevel" -> request.initialRecursionLevel,
        "includeExternalLinks" -> request.includeExternalLinks
      )
    }
  }

  implicit object CrawlRequestBSONReader extends BSONDocumentReader[CrawlRequest] {
    def read(doc: BSONDocument): CrawlRequest = {
      CrawlRequest(
        id = MongoHelper.stringifyIfSome(doc.getAs[BSONObjectID]("_id")),
        origin = doc.getAs[String]("origin").get,
        initialRecursionLevel = doc.getAs[Int]("initialRecursionLevel").get,
        includeExternalLinks = doc.getAs[Boolean]("includeExternalLinks").get
      )
    }
  }
}