package crakken.actor

import akka.actor._
import akka.event._
import akka.pattern.ask
import akka.util.{Timeout, ByteString}
import crakken.data.model.PageFetchRequest
import crakken.data.repository.GridFsMessages
import crakken.utils.JSoupExtensions.implicits._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import reactivemongo.bson.BSONDocument
import scala.collection.JavaConversions._
import spray.client.pipelining._
import scala.concurrent._
import scala.concurrent.duration._
import spray.http.HttpResponse
import spray.http.Uri
import scala.util.Success
import ExecutionContext.Implicits.global

import scala.language.postfixOps

object PageFetchActor {
  def props(pipeline: SendReceive, repositoryActor: ActorRef) = Props { new PageFetchActor(pipeline, repositoryActor) }
}
class PageFetchActor(pipeline: SendReceive, repositoryActor: ActorRef) extends Actor with ActorLogging with UnboundedStash{

  implicit val timeout = Timeout(5 seconds)  // for ask pattern

  def receive = LoggingReceive {
    case request: PageFetchRequest  => {
      val replyTo = sender
      val composedFuture = for {
        response <- pipeline { Get(Uri(request.url)) }
        _ <- Future { followRedirect(response, request, replyTo) }
        requestWithStatus <- Future { request.copy(statusCode = Some(response.status.intValue))}
        parsedDoc <- Future { Jsoup.parse(response.entity.asString) }
        normalizedDoc <- Future { normalizeLinks(parsedDoc, Uri(requestWithStatus.url)) }
        GridFsMessages.created(Success(documentId)) <-
                  repositoryActor ? GridFsMessages.create(ByteString(normalizedDoc.html),
                                                          request.url,
                                                          getContentType(response),
                                                          BSONDocument())
        requestWithContent <- Future { requestWithStatus.copy(contentId = Some(documentId))}
        _ <- Future { recurseLinks(normalizedDoc, requestWithContent, replyTo) }
        result <- Future { replyTo ! PageFetchSuccess(requestWithContent) }
      } yield result

      composedFuture recoverWith {
        case (ex: Throwable) => Future { replyTo ! PageFetchFailure(request, ex) }
      }
    }
  }

  def getContentType(response: HttpResponse) : String = {
    response.headers.filter(header => header.name.toLowerCase == "content-type").map(header => header.value).headOption.getOrElse("text/html")
  }

  def followRedirect(response: HttpResponse, request: PageFetchRequest, replyTo: ActorRef) = {
    log.info(s"${response.status.intValue} - ${request.recursionLevel} - ${request.url}")
    if ((response.status.intValue >= 300) && (response.status.intValue < 400) && (request.recursionLevel > 0)) {
      response.headers
        .filter(header => header.name.toLowerCase == "location")
        .map(header => header.value).headOption
        .map(location => replyTo ! PageFetchRequest(None,request.crawlRequestId,location,None,None,request.recursionLevel - 1,request.includeExternalLinks))
    }
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

  def recurseLinks(inputDocument: Document, request: PageFetchRequest, replyTo: ActorRef) = {
    val links = inputDocument.select("a[href]").iterator()
    if (request.recursionLevel > 0) {
      links.foreach(
        link => {
          val fromLink = request.url
          val toLink = link.attr("href")
          val fromHost = Uri(fromLink).authority
          val toHost = Uri(toLink).authority

          log.debug(s"Traverse from host ${fromHost.toString()} to ${toHost.toString()}")
          if (request.includeExternalLinks || (fromHost == toHost))
            replyTo ! PageFetchRequest(None, request.crawlRequestId, toLink, None, None, request.recursionLevel - 1, request.includeExternalLinks)
        }
      )
    }
  }
}

case class PageFetchSuccess(response: PageFetchRequest)
case class PageFetchFailure(response: PageFetchRequest, failure: Throwable)
