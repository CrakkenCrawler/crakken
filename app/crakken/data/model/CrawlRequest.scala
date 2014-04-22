package crakken.data.model

import reactivemongo.bson._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class CrawlRequest(id: String = BSONObjectID.generate.stringify, origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean )

object CrawlRequest {
  def apply(id: Option[String], origin: String, initialRecursionLevel: Int, includeExternalLinks: Boolean )  =
    new CrawlRequest(id.getOrElse(BSONObjectID.generate.stringify), origin, initialRecursionLevel, includeExternalLinks)

  implicit object CrawlRequestBSONWriter extends BSONDocumentWriter[CrawlRequest] {
    def write(request: CrawlRequest): BSONDocument = {
      BSONDocument(
        "_id" -> BSONObjectID.parse(request.id).get,
        "origin" -> request.origin,
        "initialRecursionLevel" -> request.initialRecursionLevel,
        "includeExternalLinks" -> request.includeExternalLinks
      )
    }
  }

  implicit object CrawlRequestBSONReader extends BSONDocumentReader[CrawlRequest] {
    def read(doc: BSONDocument): CrawlRequest = {
      CrawlRequest(
        id = doc.getAs[BSONObjectID]("_id").get.stringify,
        origin = doc.getAs[String]("origin").get,
        initialRecursionLevel = doc.getAs[Int]("initialRecursionLevel").get,
        includeExternalLinks = doc.getAs[Boolean]("includeExternalLinks").get
      )
    }
  }

  implicit val crawlRequestWrites: Writes[CrawlRequest] = (
      (JsPath \ "id").write[String] and
      (JsPath \ "origin").write[String] and
        (JsPath \ "initialRecursionLevel").write[Int] and
        (JsPath \ "includeExternals").write[Boolean]
    )(unlift(CrawlRequest.unapply))
}