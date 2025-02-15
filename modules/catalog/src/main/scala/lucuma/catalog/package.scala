// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma

import cats.data._
import lucuma.catalog.CatalogProblem.FieldValueProblem

package object catalog {

  def parseDoubleValue(
    ucd: Ucd,
    s:   String
  ): ValidatedNec[CatalogProblem, Double] =
    Validated
      .catchNonFatal(s.toDouble)
      .leftMap(_ => FieldValueProblem(ucd, s))
      .toValidatedNec

}
