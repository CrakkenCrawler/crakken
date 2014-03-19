package controllers

import play.api.mvc.{Action, Controller}
import play.libs.Akka
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.database.CrawlRequest

object CrawlRequestController extends Controller {

  val crawlRouter = Akka.system.actorSelection("/user/crawlRequestRouter")

  def create = Action {
      Ok(views.html.crawlrequest.index(crawlRequestForm))
  }

  def submit = Action { implicit request =>
      crawlRequestForm.bindFromRequest.fold(
        errors => BadRequest(views.html.crawlrequest.index(errors)),
        crawlRequest => {

          crawlRouter ! crawlRequest
          Ok(views.html.crawlrequest.submit())
        }
      )
  }

  def list = TODO

  def get(id: Long) = TODO

  val crawlRequestForm: Form[CrawlRequest] =
    Form(
      mapping(
        "id" -> optional(of[Long]),
        "origin" -> text,
        "initialRecursionLevel" -> number,
        "includeExternalLinks" -> boolean
      ) (CrawlRequest.apply) (CrawlRequest.unapply)
    )
}
