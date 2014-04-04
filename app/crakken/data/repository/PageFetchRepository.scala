package crakken.data.repository

import scala.concurrent._
import crakken.data.model.PageFetchRequest
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import scala.util.{Success, Try}
import crakken.utils.MongoHelper

trait PageFetchRequestRepositoryComponent {

  val pageFetchRequestRepository: PageFetchRequestRepository

  trait PageFetchRequestRepository {
    def create(request: PageFetchRequest) : Future[PageFetchRequest]
    def update(request: PageFetchRequest) : Future[Int]
    def getById(id: String): Future[Option[PageFetchRequest]]
    def getAll(): Future[List[PageFetchRequest]]
  }

  class MongoPageFetchRequestRepository extends PageFetchRequestRepository {
    import play.modules.reactivemongo.ReactiveMongoPlugin
    import play.api.Play.current
    import ExecutionContext.Implicits.global
    import PageFetchRequest._

    def db = ReactiveMongoPlugin.db
    lazy val collection = db[BSONCollection]("pageFetchRequests")

    def create(request: PageFetchRequest) = {
      val doc = PageFetchRequestBSONWriter.write(request)
      for {
        lastError <- collection.insert(doc)
        response <- Future { PageFetchRequestBSONReader.read(doc) }
        _ <- Future { println(s"Database actor created ${response}")}
      } yield response
    }

    def update(request: PageFetchRequest) = {
      val doc = PageFetchRequestBSONWriter.write(request)
      val id = doc.getAs[BSONObjectID]("_id").get
      val modifier = BSONDocument(doc.stream.filter(
        //filter out the _id field as mongo wont let you change an id once created
        _ match {
          case Success(("_id",_)) => false
          case _ => true
        }))
      for {
        lastError <- collection.update(BSONDocument("_id" -> id), BSONDocument("$set" -> modifier))
      } yield lastError.updated
    }

    def getById(id: String) =  for {
      list <- get(BSONDocument("_id" -> id))
    } yield list.headOption

    def getAll() =  get(BSONDocument())

    def get(q: BSONDocument) = {
      val found = collection.find(q)
      found.cursor[PageFetchRequest].collect[List]()
    }
  }

  class MockPageFetchRequestRepository extends PageFetchRequestRepository {
    def create(request: PageFetchRequest) = ???
    def update(request: PageFetchRequest) = ???
    def getById(id: String) = ???
    def getAll() = ???
  }
}

object PageFetchRequestMessages {
  case class create(request: PageFetchRequest)
  case class update(request: PageFetchRequest)
  case class getById(id: String)
  case class getAll()

  case class created(response: Try[PageFetchRequest])
  case class updated(response: Try[Int])
  case class gotById(response: Try[Option[PageFetchRequest]])
  case class gotAll(responses: Try[List[PageFetchRequest]])
}