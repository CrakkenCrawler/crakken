package actors

import akka.actor._
import akka.event._
import akka.pattern.pipe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.concurrent._
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse, Uri}
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import play.api.db.slick._
import scala.collection.JavaConversions._

import utils.JsoupExtensions._
import models.database._

import implicits._
import spray.http.HttpRequest
import spray.http.HttpResponse
import models.database.PageFetchRequest
import models.database.CrawlRequest

object PageFetchActor {
  def props(): Props = Props[PageFetchActor]
}

class PageFetchActor extends Actor with UnboundedStash{
  import ExecutionContext.Implicits.global

  val log = Logging(context.system, this)
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  val databaseService = context.actorSelection("")

  def receive = idle

  def idle: Receive = LoggingReceive {
    case request: PageFetchRequest  => {
      val crawlRequestActor = context.actorSelection("../..")
      DB("crakken") withSession { implicit session =>
        val pageFetchRequests = TableQuery[PageFetchRequests]
        val returnedId = (pageFetchRequests returning pageFetchRequests.map(_.id)) += request
        val requestWithId = request.copy(id = returnedId)

        val composedFuture = for {
          response <- pipeline { Get(Uri(requestWithId.url)) }
          _ <- Future(followRedirect(response, requestWithId))
          requestWithStatus <- Future(updateRequest(requestWithId.copy(statusCode = Some(response.status.intValue))))
          parsedDoc <- Future(Jsoup.parse(response.entity.asString))
          normalizedDoc <- Future(normalizeLinks(parsedDoc, Uri(request.url)))
          _ <- Future(updateRequest(requestWithStatus.copy(content = Some(normalizedDoc.html))))
          _ <- Future(recurseLinks(normalizedDoc, request, crawlRequestActor))
          result <- Future(crawlRequestActor ! PageFetchSuccess(normalizedDoc, requestWithId.url))

        } yield result

        val futureWithRecovery = composedFuture recoverWith {
          case (ex: Throwable) => Future(crawlRequestActor ! PageFetchFailure(ex, request.url))
        }
        //composedFuture ! crawlRequestActor
      }
    }
  }

  def followRedirect(response: HttpResponse, request: PageFetchRequest) : HttpResponse = {
    if ((response.status.intValue >= 300) && (response.status.intValue < 400)) {
      val locations = response.headers.filter(header => header.is("location"))
      locations.foreach(location => {
        val newRequest = PageFetchRequest(
          None,
          request.crawlRequestId,
          location.value,
          None,
          None,
          request.recursionLevel - 1,
          request.includeExternalLinks)
        context.parent.tell(newRequest, context.parent)
      } )
    }
    response
  }

  def normalizeLinks(inputDocument: Document, baseUri: Uri): Document = {
      inputDocument
        .makeAbsolute("a[href]", "href")(baseUri)
        .makeAbsolute("img[src]", "src")(baseUri)
        .makeAbsolute("img[href]", "href")(baseUri)
        .makeAbsolute("script[src]", "src")(baseUri)
        .makeAbsolute("meta[itemprop=image]", "content")(baseUri)
        .makeAbsolute("link[href]", "href")(baseUri)
        .makeAbsolute("form[action]", "action")(baseUri)
        .makeAbsolute("source[src]", "src")(baseUri)
  }

  def updateRequest(request: PageFetchRequest)(implicit session: Session) : PageFetchRequest = {
    DB("crakken") withSession { implicit session =>
      val pageFetchRequests = TableQuery[PageFetchRequests]
      pageFetchRequests.where(_.id === request.id.get).update(request)
    }
    request
  }

  def recurseLinks(inputDocument: Document, request: PageFetchRequest, crawlRequestActor: ActorSelection) :Document = {
    val links = inputDocument.select("a[href]").iterator()
    if (request.recursionLevel > 0) {
      links.foreach(link => {
        val fromLink = request.url
        val toLink = link.attr("href")
        val fromHost = Uri(fromLink).authority
        val toHost = Uri(toLink).authority

        log.debug(s"Traverse from host ${fromHost.toString} to ${toHost.toString}")
        if ((request.includeExternalLinks) || (fromHost == toHost)) {
          val newRequest = PageFetchRequest(
            None,
            request.crawlRequestId,
            toLink,
            None,
            None,
            request.recursionLevel - 1,
            request.includeExternalLinks)
          crawlRequestActor ! newRequest
        }
      })
    }
    inputDocument
  }
}

case class PageFetchSuccess(response: Document, location: String)
case class PageFetchFailure(failure: Throwable, location: String)
