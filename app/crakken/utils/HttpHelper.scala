package crakken.utils

import spray.http.Uri
import org.jsoup.nodes.Document
import crakken.utils.JSoupExtensions.implicits._

object HttpHelper {
  def normalizeDocument(document: Document, baseUri: Uri) : Document = {
    document
      .makeAbsolute("a[href]", "href")(baseUri)
      .makeAbsolute("img[src]", "src")(baseUri)
      .makeAbsolute("img[href]", "href")(baseUri)
      .makeAbsolute("script[src]", "src")(baseUri)
      .makeAbsolute("meta[itemprop=image]", "content")(baseUri)
      .makeAbsolute("link[href]", "href")(baseUri)
      .makeAbsolute("form[action]", "action")(baseUri)
      .makeAbsolute("source[src]", "src")(baseUri)
  }
}
