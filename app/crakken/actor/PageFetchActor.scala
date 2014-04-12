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
import crakken.utils.HttpHelper
import java.io.ByteArrayInputStream

object PageFetchActor {
  def props(pipeline: SendReceive, repositoryActor: ActorRef) = Props { new PageFetchActor(pipeline, repositoryActor) }
}
class PageFetchActor(pipeline: SendReceive, repositoryActor: ActorRef) extends Actor with ActorLogging with UnboundedStash{

  implicit val timeout = Timeout(10 seconds)  // for ask pattern

  def receive = LoggingReceive {
    case request: PageFetchRequest  => {
      val replyTo = sender
      for {

        //Fetch and store the original content
        response <- pipeline { Get(Uri(request.url)) }
        _ <- Future { followRedirect(response, request, replyTo) }
        contentTypeHeader <- Future { getContentTypeHeader(response) }

        //TODO: Need to pull out content type only
        encoding <- Future { getEncoding(contentTypeHeader) }
        contentType <- Future { getContentType(contentTypeHeader) }
        GridFsMessages.created(Success(documentId)) <- repositoryActor ? GridFsMessages.create(response.entity.data.toByteString,
                                                                                                request.url,
                                                                                                contentTypeHeader,
                                                                                                BSONDocument())
        completedRequest <- Future { request.copy(contentId = Some(documentId), statusCode = Some(response.status.intValue))}
        _ <- Future { replyTo ! PageFetchSuccess(completedRequest) }

        //Skim the content for new links
        parsedDocument <- Future { Jsoup.parse(new ByteArrayInputStream(response.entity.data.toByteArray), encoding, request.url) }  if contentType == "text/html"
        normalizedDocument <- Future { parsedDocument.makeAbsolute("a[href]", "href")(request.url) }
        linkList <- Future {  normalizedDocument.select("a[href]").iterator().map(element => element.attr("href")).toList }
        result <- Future { recurseLinks(linkList, completedRequest, replyTo) }
      } yield result
    }
  }

  def getContentTypeHeader(response: HttpResponse) = {
    response.headers.filter(header => header.name.toLowerCase == "content-type").map(header => header.value).headOption.getOrElse("text/html; charset=UTF-8")
  }

  def getEncoding(contentTypeHeader: String) : String = {
    val tokens = contentTypeHeader.split(';').toList
    val params = tokens.map(token => (token.split(';').head, token.split(';').tail.headOption))
    val encodings = params.filter(param => param._1.toLowerCase == "charset").map(charset => charset._2).filter(encoding => encoding.nonEmpty).map(encoding => encoding.get)
    encodings.headOption.getOrElse("UTF-8")
  }

  def getContentType(contentTypeHeader: String) : String = {
    contentTypeHeader.split(';').toList.headOption.getOrElse("text/html")
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

  def recurseLinks(links: List[String], request: PageFetchRequest, replyTo: ActorRef) = {
    if (request.recursionLevel > 0) {
      links.foreach(
        toLink => {
          val fromLink = request.url
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
