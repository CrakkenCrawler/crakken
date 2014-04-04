package crakken.utils

import reactivemongo.bson.BSONObjectID

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
}
