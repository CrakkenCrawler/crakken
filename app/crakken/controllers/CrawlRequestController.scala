package crakken.controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import crakken.data.model.CrawlRequest
import play.modules.reactivemongo.MongoController
import crakken.Global
import crakken.data.repository.CrawlRequestMessages
import akka.pattern.ask
import scala.util._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import crakken.utils.DataTablesHelper

object CrawlRequestController extends Controller with MongoController {
  implicit val timeout = Timeout(10 seconds)

  val crawlRequestRouter = Akka.system.actorSelection(Global.crawlRequestRouterPathName)
  val repositoryRouter = Akka.system.actorSelection(Global.repositoryRouterPathName)

  def create = Action {
      Ok(views.html.crawlrequest.create(crawlRequestForm))
  }

  def submit = Action { implicit request =>
      crawlRequestForm.bindFromRequest.fold(
        errors => BadRequest(views.html.crawlrequest.create(errors)),
        crawlRequest => {
          crawlRequestRouter ! crawlRequest
          Ok(views.html.crawlrequest.submit(crawlRequest))
        }
      )
  }

  def index = Action.async { request =>
    val dbResponse = repositoryRouter ? CrawlRequestMessages.getAll
    dbResponse map {
      case CrawlRequestMessages.gotAll(Success(crawlRequests)) => Ok(views.html.crawlrequest.index(crawlRequests))
      case CrawlRequestMessages.gotAll(Failure(ex)) => BadRequest(ex.toString)
      case response => BadRequest(response.toString)
    }
  }

  def get(id: String) = Action {request =>
    Ok(views.html.crawlrequest.get(id))
  }

  def list = Action.async { request =>
    val dbResponse = repositoryRouter ? CrawlRequestMessages.getAll
    dbResponse map {
      case CrawlRequestMessages.gotAll(Success(crawlRequests)) => Ok(DataTablesHelper.wrapJson(crawlRequests))
      case CrawlRequestMessages.gotAll(Failure(ex)) => BadRequest(ex.toString)
      case response => BadRequest(response.toString)
    }
  }

  /*def status(id: String) = WebSocket.async[JsValue] {

  } */

  val crawlRequestForm: Form[CrawlRequest] =
    Form(
      mapping(
        "id" -> optional(text),
        "origin" -> text,
        "initialRecursionLevel" -> number,
        "includeExternalLinks" -> boolean
      ) (CrawlRequest.apply) (request => Some((Some(request.id), request.origin, request.initialRecursionLevel, request.includeExternalLinks)))
    )
}
