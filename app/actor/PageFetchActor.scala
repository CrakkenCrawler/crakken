package actor

import akka.actor._
import akka.event._
import utils.JSoupExtensions.implicits._
import models.database._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._
import scala.concurrent._
import spray.client.pipelining._
import spray.http.HttpResponse
import spray.http.Uri

class PageFetchActor(pipeline: SendReceive) extends Actor with UnboundedStash{

  import ExecutionContext.Implicits.global

  val log = Logging(context.system, this)

  def receive = idle

  def idle: Receive = LoggingReceive {
    case request: PageFetchRequest  => {
      val replyTo = sender
      val composedFuture = for {
        response <- pipeline { Get(Uri(request.url)) }
        _ <- Future(followRedirect(response, request, replyTo))
        requestWithStatus <- Future(request.copy(statusCode = Some(response.status.intValue)))
        parsedDoc <- Future(Jsoup.parse(response.entity.asString))
        normalizedDoc <- Future(normalizeLinks(parsedDoc, Uri(requestWithStatus.url)))
        requestWithContent <- Future(requestWithStatus.copy(content = Some(normalizedDoc.html)))
        _ <- Future(recurseLinks(normalizedDoc, requestWithContent, replyTo))
        result <- Future(replyTo ! PageFetchSuccess(requestWithContent))

      } yield result

      composedFuture recoverWith {
        case (ex: Throwable) => Future(replyTo ! PageFetchFailure(request, ex))
      }

    }
  }

  def followRedirect(response: HttpResponse, request: PageFetchRequest, replyTo: ActorRef) : HttpResponse = {
    if ((response.status.intValue >= 300) && (response.status.intValue < 400)) {
      val locations = response.headers.filter(header => header.is("location"))
      locations.foreach(
        location => {
          val newRequest = PageFetchRequest(
            None,
            request.crawlRequestId,
            location.value,
            None,
            None,
            request.recursionLevel - 1,
            request.includeExternalLinks)
          replyTo ! newRequest
        }
      )
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

  def recurseLinks(inputDocument: Document, request: PageFetchRequest, replyTo: ActorRef) :Document = {
    val links = inputDocument.select("a[href]").iterator()
    if (request.recursionLevel > 0) {
      links.foreach(
        link => {
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
            replyTo ! newRequest
          }
        }
      )
    }
    inputDocument
  }
}

case class PageFetchSuccess(response: PageFetchRequest)
case class PageFetchFailure(response: PageFetchRequest, failure: Throwable)
