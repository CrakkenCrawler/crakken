package crakken.actor

import akka.actor._
import akka.event._
import akka.pattern.ask
import akka.util.Timeout
import crakken.data.model.PageFetchRequest
import crakken.data.repository.GridFsMessages
import org.jsoup.Jsoup
import reactivemongo.bson.BSONDocument
import scala.collection.JavaConversions._
import spray.client.pipelining._
import scala.concurrent._
import scala.concurrent.duration._
import spray.http.Uri
import scala.util.Success
import ExecutionContext.Implicits.global

import scala.language.postfixOps
import crakken.utils.PageFetchHelper
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
        _ <- Future { PageFetchHelper.followRedirect(response, request, replyTo) }
        contentTypeHeader <- Future { PageFetchHelper.getContentTypeHeader(response) }
        encoding <- Future { PageFetchHelper.getEncoding(contentTypeHeader) }
        contentType <- Future { PageFetchHelper.getContentType(contentTypeHeader) }
        GridFsMessages.created(Success(documentId)) <- repositoryActor ? GridFsMessages.create(response.entity.data.toByteString,
                                                                                                request.url,
                                                                                                contentTypeHeader,
                                                                                                BSONDocument())
        completedRequest <- Future { request.copy(contentId = Some(documentId), statusCode = Some(response.status.intValue))}
        _ <- Future { replyTo ! PageFetchSuccess(completedRequest) }

        //Skim the content for new links
        parsedDocument <- Future { Jsoup.parse(new ByteArrayInputStream(response.entity.data.toByteArray), encoding, request.url) }  if contentType == "text/html"
        linkList <- Future {  parsedDocument.select("a[href]").iterator().map(element => element.attr("abs:href")).toList }
        result <- Future { PageFetchHelper.recurseLinks(linkList, completedRequest, replyTo) }
      } yield result
    }
  }

}

case class PageFetchSuccess(response: PageFetchRequest)
case class PageFetchFailure(response: PageFetchRequest, failure: Throwable)
