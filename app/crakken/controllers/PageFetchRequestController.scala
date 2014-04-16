package crakken.controllers

import play.modules.reactivemongo.MongoController
import play.libs.Akka
import crakken.Global
import play.api.mvc._
import play.api._
import crakken.data.repository.{GridFsMessages, PageFetchRequestMessages}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Failure
import scala.Some
import play.api.libs.iteratee._
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import crakken.utils.PageFetchHelper

object PageFetchRequestController extends Controller with MongoController {
  implicit val timeout = Timeout(10 seconds)

  val repositoryRouter = Akka.system.actorSelection(Global.repositoryRouterPathName)

  def get(id: String) = Action.async { request =>
    val composedFuture = for {
        PageFetchRequestMessages.gotById(Success(Some(pageFetchRequest))) <- repositoryRouter ? PageFetchRequestMessages.getById(id)
        GridFsMessages.gotById(Success((rawEnumerator,contentTypeHeader))) <- repositoryRouter ? GridFsMessages.getById(pageFetchRequest.contentId.get)
        maybeNormalizedEnumerator <- Future {
          if (PageFetchHelper.getContentType(contentTypeHeader).toLowerCase() == "text/html") {
            rawEnumerator &> normalizeRefs(PageFetchHelper.getEncoding(contentTypeHeader), pageFetchRequest.url)
          } else {
            rawEnumerator
          }
        }
      } yield Ok.chunked(maybeNormalizedEnumerator).as(contentTypeHeader)
    composedFuture recover {
      case ex: Throwable => {
        Logger.error("Exception thrown", ex);
        BadRequest(ex.toString)
      }
    }
  }


  def listByCrId(id: String) = Action.async { request =>
    val dbResponse = repositoryRouter ? PageFetchRequestMessages.getByCrId(id)
    dbResponse map {
      case PageFetchRequestMessages.gotByCrId(Success(pageFetchRequests)) => Ok(views.html.pagefetchrequest.getbycrid(pageFetchRequests,id))
      case PageFetchRequestMessages.gotByCrId(Failure(ex)) => BadRequest(ex.toString)
    }
  }

  def encodeToString(charset: String) : Enumeratee[Array[Byte], String] = Enumeratee.map( bytes => new String(bytes, charset))

  def decodeToBytes(charset: String) : Enumeratee[String, Array[Byte]] =  Enumeratee.map(str => str.getBytes(charset))

  def toJSoup(baseUri: String) : Enumeratee[String, Document] = Enumeratee.grouped(Iteratee.consume[String]()) ><> Enumeratee.map(str => Jsoup.parse(str, baseUri))
  val fromJSoup: Enumeratee[Document, String] = Enumeratee.map(doc => doc.html)
  val normalize: Enumeratee[Document, Document] = Enumeratee.map(doc => PageFetchHelper.normalizeDocument(doc))

  def tee[T]: Enumeratee[T, T] = Enumeratee.map(value => {Logger.info(s"TEED: ${value.toString}"); value} )

  def normalizeRefs(charset: String, baseUri: String) : Enumeratee[Array[Byte], Array[Byte]] =
    encodeToString(charset) ><>
    toJSoup(baseUri) ><>
    normalize ><>
    fromJSoup ><>
    decodeToBytes(charset)
}
