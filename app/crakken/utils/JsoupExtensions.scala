package crakken.utils

import org.jsoup.nodes.Document
import spray.http.Uri
import scala.collection.JavaConversions._
import scala.language.implicitConversions

object JSoupExtensions {
  class NormalizableJSoupDocument(document: Document) {
    def makeAbsolute(selector: String, attribute: String): Document = {
      val outputDocument = document.clone()
      document.location()
      outputDocument.select(selector).iterator.foreach(link => link.attr(attribute, link.absUrl(attribute)))
      outputDocument
    }
  }

  object implicits {
    implicit def normalizableDocument(document: Document) = new NormalizableJSoupDocument(document)
  }

}