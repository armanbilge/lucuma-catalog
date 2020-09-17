// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.catalog.votable

import cats.implicits._
import munit.DisciplineSuite
import cats.kernel.laws.discipline.EqTests
import lucuma.catalog._
import lucuma.catalog.arb.ArbUcd._
import cats.data.NonEmptyList
import eu.timepit.refined._
import eu.timepit.refined.collection._

class UcdSuite extends munit.FunSuite with DisciplineSuite {
  checkAll("Ucd", EqTests[Ucd].eqv)

  test("detect if is a superset") {
    assert(
      Ucd.unsafeFromString("stat.error;phot.mag;em.opt.i").includes(refineMV[NonEmpty]("phot.mag"))
    )
    assert(Ucd.unsafeFromString("stat.error;phot.mag;em.opt.i").matches("phot.mag".r))
    assert(Ucd.unsafeFromString("stat.error;phot.mag;em.opt.i").matches("em.opt.(\\w)".r))
  }
  test("parse single token ucds") {
    assertEquals(Ucd.parseUcd("meta.code"), Ucd(refineMV[NonEmpty]("meta.code")).some)
  }
  test("parse multi token ucds and preserve order") {
    assertEquals(
      Ucd.parseUcd("stat.error;phot.mag;em.opt.g"),
      Ucd(
        NonEmptyList.of(refineMV[NonEmpty]("stat.error"),
                        refineMV[NonEmpty]("phot.mag"),
                        refineMV[NonEmpty]("em.opt.g")
        )
      ).some
    )
  }
  test("parse be case-insensitive, converting to lower case") {
    assertEquals(Ucd.parseUcd("STAT.Error;EM.opt.G"), Ucd("stat.error;em.opt.g"))
  }
}
