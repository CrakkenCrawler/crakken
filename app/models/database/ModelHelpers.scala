package models.database

import play.api.db.slick.Config.driver.simple._
import scala.util._

import scala.slick.lifted.CanBeQueryCondition

case class CreateEntity[A, B <: Table[A]](entity: A, q: TableQuery[B])
case class Created[A](entity: Try[A])

case class UpdateEntities[A, B <: Table[A]](filter: B => Boolean, transform: B => A, q: TableQuery[B])
case class Updated(count: Try[Int])

case class DeleteEntities[B <: Table[A] forSome {type A}](filter: B => Boolean, q: TableQuery[B])
case class Deleted(count: Try[Int])

case class GetEntities[B <: Table[A] forSome {type A}](filter: B => Boolean, q: TableQuery[B])
case class Entities[A](results: Try[List[A]])