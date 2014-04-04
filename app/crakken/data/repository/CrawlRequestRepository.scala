package crakken.data.repository

import crakken.data.model.CrawlRequest
import scala.util.Try
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.bson.BSONDocument

import ExecutionContext.Implicits.global

trait CrawlRequestRepositoryComponent {

  val crawlRequestRepository: CrawlRequestRepository

  trait CrawlRequestRepository {
    def create(request: CrawlRequest) : Future[CrawlRequest]
    def getById(id: String): Future[Option[CrawlRequest]]
    def getAll(): Future[List[CrawlRequest]]
  }

  class MongoCrawlRequestRepository extends CrawlRequestRepository {
    import play.modules.reactivemongo.ReactiveMongoPlugin
    import play.api.Play.current
    import CrawlRequest._

    def db = ReactiveMongoPlugin.db
    lazy val collection = db[BSONCollection]("crawlRequests")

    def create(request: CrawlRequest) = {
      val doc = CrawlRequestBSONWriter.write(request)
      for {
        lastError <- collection.insert(doc)
        response <- Future {CrawlRequestBSONReader.read(doc)}
      } yield response
    }

    def getById(id: String) =  for {
      list <- get(BSONDocument("_id" -> id))
      result <- Future { list.headOption }
    } yield result

    def getAll() =  get(BSONDocument())

    def get(q: BSONDocument) = {
      val found = collection.find(q)
      found.cursor[CrawlRequest].collect[List]()
    }
  }

  class MockCrawlRequestRepository extends CrawlRequestRepository {
    def create(request: CrawlRequest) = Future { request.copy(id = Some("123abc"))}
    def getById(id: String) = Future {
      id match {
        case "123abc" => Some(CrawlRequest(Some("123abc"), "http://www.google.com", 1, false))
        case _ => None
      }
    }
    def getAll() = Future { List(CrawlRequest(Some("123abc"), "http://www.google.com", 1, false)) }
  }
}

object CrawlRequestMessages {
  case class create(request: CrawlRequest)
  case class getById(id: String)
  case class getAll()

  case class created(response: Try[CrawlRequest])
  case class gotById(response: Try[Option[CrawlRequest]])
  case class gotAll(responses: Try[List[CrawlRequest]])
}