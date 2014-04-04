package crakken.test

import scala.concurrent._
import spray.client.pipelining._
import spray.http.{StatusCode, HttpResponse}

/**
 * Created by 601292 on 4/4/2014.
 */
object FetchMocker {

  import ExecutionContext.Implicits.global

  def apply(status: Integer, response: String): SendReceive = {
    request => future { HttpResponse(status = StatusCode.int2StatusCode(status),entity = response) }
  }
}
