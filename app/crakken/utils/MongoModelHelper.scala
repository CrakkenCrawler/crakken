package crakken.utils

import reactivemongo.bson.BSONObjectID
import scala.util.{Success, Failure}

object MongoHelper {
  def generateOrParse(maybeId: Option[String]): BSONObjectID = {
    (for {
      id <- maybeId
      bsonId <- Some(BSONObjectID(id))
    } yield bsonId) getOrElse BSONObjectID.generate
  }
  def stringifyIfSome(maybeId: Option[BSONObjectID]): Option[String] = {
    for {
      oid <- maybeId
      strId <- Some(oid.stringify)
    } yield strId
  }

  def parseIdIfSome(maybeId: Option[String]): Option[BSONObjectID] = {
    maybeId match {
      case None => None
      case Some(strId) => BSONObjectID.parse(strId) match {
        case Failure(ex: Throwable) => None
        case Success(oid) => Some(oid)
      }
    }
  }
}
