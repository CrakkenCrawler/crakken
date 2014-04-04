package crakken.controllers

import play.api.mvc.{Action, Controller}
import play.libs.Akka
import play.api.data.Form
import play.api.data.Forms._
import crakken.data.model.CrawlRequest
import play.modules.reactivemongo.MongoController
import crakken.Global

object CrawlRequestController extends Controller with MongoController {

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
          Ok(views.html.crawlrequest.submit())
        }
      )
  }

  def index = Action { implicit request =>
    Ok(views.html.crawlrequest.index(List()))
  }

  def get(id: String) = TODO

  val crawlRequestForm: Form[CrawlRequest] =
    Form(
      mapping(
        "id" -> optional(text),
        "origin" -> text,
        "initialRecursionLevel" -> number,
        "includeExternalLinks" -> boolean
      ) (CrawlRequest.apply) (CrawlRequest.unapply)
    )
}
