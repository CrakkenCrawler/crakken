package crakken.data.repository

import scala.concurrent._
import crakken.data.model.PageFetchRequest
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import scala.util.{Success, Try}
import reactivemongo.api.gridfs.GridFS
import play.Logger
import reactivemongo.api.QueryOpts

trait PageFetchRequestRepositoryComponent {

  val pageFetchRequestRepository: PageFetchRequestRepository

  trait PageFetchRequestRepository {
    def create(request: PageFetchRequest)
    def update(request: PageFetchRequest)
    def getById(id: String): Future[Option[PageFetchRequest]]
    def getByCrId(id: String): Future[List[PageFetchRequest]]
    def getAll: Future[List[PageFetchRequest]]
  }

  class MongoPageFetchRequestRepository extends PageFetchRequestRepository {

    import play.modules.reactivemongo.ReactiveMongoPlugin
    import play.api.Play.current
    import ExecutionContext.Implicits.global
    import PageFetchRequest._

    def db = ReactiveMongoPlugin.db

    lazy val collection = db[BSONCollection]("pageFetchRequests")
    val gridFS = new GridFS(db)

    // let's build an index on our gridfs chunks collection if none
    gridFS.ensureIndex()

    def create(request: PageFetchRequest) = {
      val doc = PageFetchRequestBSONWriter.write(request)
      collection.insert(doc)
    }

    def update(request: PageFetchRequest) = {
      val doc = PageFetchRequestBSONWriter.write(request)
      val id = doc.getAs[BSONObjectID]("_id").get
      val modifier = BSONDocument(doc.stream.filter(
        //filter out the _id field as mongo wont let you change an id once created
        _ match {
          case Success(("_id", _)) => false
          case _ => true
        }))

      collection.update(BSONDocument("_id" -> id), BSONDocument("$set" -> modifier))
    }

    def getById(id: String) =
    for {
      list <- get(BSONDocument("_id" -> BSONObjectID.parse(id).get))
    } yield list.headOption


    def getByCrId(id: String) =  for {
      list <- get(BSONDocument("crawlRequestId" -> BSONObjectID.parse(id).get))
    } yield list

    def getAll() =  get(BSONDocument())

    def get(q: BSONDocument = BSONDocument(), sort: BSONDocument = BSONDocument(), queryOpts: QueryOpts = QueryOpts()) = {
      val found = collection.find(q).sort(sort).options(queryOpts)
      found.cursor[PageFetchRequest].collect[List]()
    }
  }

  class MockPageFetchRequestRepository extends PageFetchRequestRepository {
    def create(request: PageFetchRequest) = ???
    def update(request: PageFetchRequest) = ???
    def getById(id: String) = ???
    def getByCrId(id: String) = ???
    def getAll() = ???
  }
}

object PageFetchRequestMessages {
  case class create(request: PageFetchRequest)
  case class update(request: PageFetchRequest)
  case class getById(id: String)
  case class getByCrId(id: String)
  case class getAll(includeContent: Boolean)

  case class gotById(response: Try[Option[PageFetchRequest]])
  case class gotByCrId(response: Try[List[PageFetchRequest]])
  case class gotAll(responses: Try[List[PageFetchRequest]])
}