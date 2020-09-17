// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.catalog

import cats._
import cats.implicits._
import scala.util.matching.Regex
import cats.data.NonEmptyList
import eu.timepit.refined._
import eu.timepit.refined.cats._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string._

final case class Ucd(tokens: NonEmptyList[NonEmptyString]) {
  def includes(ucd: NonEmptyString): Boolean = tokens.exists(_ === ucd)

  def matches(r: Regex): Boolean = tokens.exists(t => r.findFirstIn(t.value).isDefined)

  override def toString = tokens.map(_.value).mkString_(", ")
}

object Ucd {
  def parseUcd(v: String): Option[Ucd] =
    v.split(";").filter(_.nonEmpty).map(_.toLowerCase).toList match {
      case h :: tail =>
        (refineV[NonEmpty](h), tail.traverse(refineV[NonEmpty](_))) match {
          case (Right(h), Right(t)) =>
            Ucd(NonEmptyList.of(h, t: _*)).some
          case _                    => none
        }
      case _         => none
    }

  def apply(ucd: String): Option[Ucd] = parseUcd(ucd)

  def apply(ucd: NonEmptyString): Ucd = Ucd(NonEmptyList.of(ucd))

  def unsafeFromString(ucd: String): Ucd = parseUcd(ucd).getOrElse(sys.error(s"Invalid ucd $ucd"))

  implicit val eqUcd: Eq[Ucd] = Eq.by(_.tokens)
}
