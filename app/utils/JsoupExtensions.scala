package utils

import org.jsoup.nodes.Document
import spray.http.Uri
import scala.collection.JavaConversions._

/**
 * Created by 601292 on 3/6/14.
 */
object JsoupExtensions {
  object implicits {
    implicit class CrakkaJSoupDocument(document: Document) {
      def makeAbsolute(selector: String, attribute: String)(baseUri: Uri): Document = {
        val outputDocument = document.clone()
        outputDocument.select(selector).iterator.foreach(link => link.attr(attribute, Uri.parseAndResolve(link.attr(attribute),baseUri).toString))
        outputDocument
      }
    }
  }

}
