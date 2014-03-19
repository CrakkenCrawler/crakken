package utils

import org.jsoup.nodes.Document
import spray.http.Uri
import scala.collection.JavaConversions._
import scala.language.implicitConversions

object JSoupExtensions {
  class NormalizableJSoupDocument(document: Document) {
    def makeAbsolute(selector: String, attribute: String)(baseUri: Uri): Document = {
      val outputDocument = document.clone()
      outputDocument.select(selector).iterator.foreach(link => link.attr(attribute, Uri.parseAndResolve(link.attr(attribute), baseUri).toString()))
      outputDocument
    }
  }

  object implicits {
    implicit def normalizableDocument(document: Document) = new NormalizableJSoupDocument(document)
  }

}