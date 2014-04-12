package crakken.data.repository

import akka.util.ByteString
import crakken.data.model.PageFetchRequest
import java.security.MessageDigest
import org.joda.time.DateTime
import reactivemongo.bson._
import scala.concurrent._
import scala.util.Try
import reactivemongo.api.gridfs._
import reactivemongo.api.gridfs.DefaultFileToSave
import scala.Some
import reactivemongo.api.gridfs.DefaultReadFile
import reactivemongo.api.gridfs.DefaultFileToSave
import scala.Some
import play.api.libs.iteratee.Enumerator

trait GridFsRepositoryComponent {

  val gridFsRepository: GridFsRepository

  trait GridFsRepository {
    def create(data: ByteString, filename: String = "", contentType: String, metadata: BSONDocument) : Future[String]
    def getById(id: String): Future[(Enumerator[Array[Byte]], String)]
  }

  class MongoGridFsRepositoryRepository extends GridFsRepository {

    import ExecutionContext.Implicits.global
    import play.api.Play.current
    import play.modules.reactivemongo.ReactiveMongoPlugin
    import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader

    val digest = MessageDigest.getInstance("MD5")

    def db = ReactiveMongoPlugin.db
    val gridFS = new GridFS(db,"pageFetchRequests")

    // let's build an index on our gridfs chunks collection if none
    gridFS.ensureIndex()

    def create(data: ByteString, filename: String = "", contentType: String = "text/html", metadata: BSONDocument = BSONDocument()) = {
      val id = BSONObjectID.generate
      val fileToSave = DefaultFileToSave(filename, Some(contentType), Some(new DateTime().getMillis),metadata, id)
      for {
        readFile <- gridFS.writeFromInputStream(fileToSave, data.iterator.asInputStream)
      } yield id.stringify
    }

    def getById(id: String) =  {
      println(s"looking up content $id")

      for {
        Some(head) <- gridFS.find[BSONDocument, ReadFile[BSONValue]] (BSONDocument("_id" -> BSONObjectID.parse(id).get)).headOption
        content <- Future { gridFS.enumerate(head) }
      } yield (content, head.contentType.getOrElse("text/html"))
    }
  }

  class MockGridFsRepository extends GridFsRepository {
    def create(data: ByteString, filename: String = "", contentType: String, metadata: BSONDocument)  = ???
    def getById(id: String) = ???
  }
}

object GridFsMessages {
  case class create(data: ByteString, filename: String = "", contentType: String = "text/plain", metadata: BSONDocument = BSONDocument())
  case class getById(id: String)

  case class created(id: Try[String])
  case class gotById(response: Try[(Enumerator[Array[Byte]],String)])
}