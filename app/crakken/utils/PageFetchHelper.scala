package crakken.utils

import org.jsoup.nodes.Document
import crakken.utils.JSoupExtensions.implicits._
import spray.http.{Uri, HttpResponse}
import crakken.data.model.PageFetchRequest
import akka.actor.ActorRef

object PageFetchHelper {
  def normalizeDocument(document: Document) : Document = {
    document
      .makeAbsolute("a[href]", "href")
      .makeAbsolute("img[src]", "src")
      .makeAbsolute("img[href]", "href")
      .makeAbsolute("script[src]", "src")
      .makeAbsolute("meta[itemprop=image]", "content")
      .makeAbsolute("link[href]", "href")
      .makeAbsolute("form[action]", "action")
      .makeAbsolute("source[src]", "src")
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

          if (request.includeExternalLinks || (fromHost == toHost))
            replyTo ! PageFetchRequest(None, request.crawlRequestId, toLink, None, None, request.recursionLevel - 1, request.includeExternalLinks)
        }
      )
    }
  }
}
