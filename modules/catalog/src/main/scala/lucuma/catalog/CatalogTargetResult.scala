// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.catalog

import cats.Eq
import lucuma.core.model.Target
import monocle.Focus
import monocle.Lens

/** Result of parsing target information from a catalog. */
final case class CatalogTargetResult(target: Target.Sidereal, angularSize: Option[AngularSize])

object CatalogTargetResult {
  val target: Lens[CatalogTargetResult, Target.Sidereal]          = Focus[CatalogTargetResult](_.target)
  val angularSize: Lens[CatalogTargetResult, Option[AngularSize]] =
    Focus[CatalogTargetResult](_.angularSize)

  /** @group Typeclass Instances */
  implicit val CatalogTargetResultEqual: Eq[CatalogTargetResult] =
    Eq.fromUniversalEquals
}
