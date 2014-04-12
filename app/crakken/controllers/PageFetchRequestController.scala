package crakken.controllers

import play.modules.reactivemongo.MongoController
import play.libs.Akka
import crakken.Global
import play.api.mvc._
import play.api._
import scala.util.{Failure, Success}
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
import views.html.defaultpages.badRequest

object PageFetchRequestController extends Controller with MongoController {
  implicit val timeout = Timeout(10 seconds)

  val repositoryRouter = Akka.system.actorSelection(Global.repositoryRouterPathName)

  def get(id: String) = Action.async { request =>
    val composedFuture = for {
        PageFetchRequestMessages.gotById(Success(Some(pageFetchRequest))) <- repositoryRouter ? PageFetchRequestMessages.getById(id)
        GridFsMessages.gotById(Success((enumerator,contentType))) <- repositoryRouter ? GridFsMessages.getById(pageFetchRequest.contentId.get)
      } yield Ok.chunked(enumerator).as(contentType)
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

}
