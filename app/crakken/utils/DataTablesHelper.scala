package crakken.utils

import play.api.libs.json._

object DataTablesHelper {
  def wrapJson[T](list: List[T])(implicit writes: Writes[T]) =
    Json.obj(
      "aaData" -> Json.toJson(list)
    )
}
