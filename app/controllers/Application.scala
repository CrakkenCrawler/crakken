package controllers

import akka.actor.ActorSystem
import play.api.mvc.{Action, Controller}
import play.libs.Akka
import actors.CrawlRequestActor
import models.database._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.database.CrawlRequest
import akka.event._

object Application extends Controller {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

  val log = Logging(Akka.system, this)
  def index = Action {
      Ok(views.html.index(crawlRequestForm))
  }

  def submitCrawlRequest = Action { implicit request =>
      crawlRequestForm.bindFromRequest.fold(
        errors => BadRequest(views.html.index(errors)),
        crawlRequest => {
          val crawlRouter = Akka.system.actorSelection("/user/crawlRequestRouter")
          crawlRouter ! crawlRequest
          Ok(views.html.submitCrawlRequest())
        }
      )

  }

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
