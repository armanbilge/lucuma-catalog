// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.catalog

import cats.effect._
import cats.data.Validated
import cats.implicits._
import coulomb._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined._
import fs2._
import fs2.io.file.Files
import fs2.io.file.Path
import munit.CatsEffectSuite
import lucuma.core.model.Target
import lucuma.core.math.RightAscension
import lucuma.core.math.Declination
import lucuma.core.math.ProperMotion
import lucuma.core.math.units.MicroArcSecondPerYear
import lucuma.core.model.AngularSize
import lucuma.core.model.Magnitude
import lucuma.core.model.CatalogId
import lucuma.core.enum.CatalogName
import lucuma.core.enum.MagnitudeSystem
import lucuma.core.math.MagnitudeValue
import lucuma.core.enum.MagnitudeBand
import lucuma.core.math.Parallax
import lucuma.core.math.RadialVelocity
import lucuma.core.math.units.KilometersPerSecond
import lucuma.core.math.HourAngle
import lucuma.core.math.Angle

class ParseSimbadFileSuite extends CatsEffectSuite with VoTableParser {

  test("parse simbad named queries") {
    // From http://simbad.u-strasbg.fr/simbad/sim-id?Ident=Vega&output.format=VOTable
    val xmlFile = "/simbad-vega.xml"
    // The sample has only one row
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // id and search name
            assertEquals(t.name, refineMV[NonEmpty]("Vega"))
            assertEquals(t.tracking.catalogId,
                         CatalogId(CatalogName.Simbad, refineMV[NonEmpty]("* alf Lyr")).some
            )
            // base coordinates
            assertEquals(
              Target.baseRA.getOption(t),
              RightAscension.fromDoubleDegrees(279.23473479).some
            )
            assertEquals(
              Target.baseDec.getOption(t),
              Declination.fromDoubleDegrees(38.78368896)
            )
            // proper motions
            assertEquals(
              Target.properMotionRA.getOption(t),
              ProperMotion.RA(200939.withUnit[MicroArcSecondPerYear]).some
            )
            assertEquals(
              Target.properMotionDec.getOption(t),
              ProperMotion.Dec(286230.withUnit[MicroArcSecondPerYear]).some
            )
            // magnitudes
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.U).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.U).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.B).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.B).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.V).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.V).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.R).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.07), MagnitudeBand.R).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.I).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.10), MagnitudeBand.I).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.J).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(-0.18), MagnitudeBand.J).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.H).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(-0.03), MagnitudeBand.H).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.K).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.13), MagnitudeBand.K).some
            )
            // parallax
            assertEquals(
              Target.parallax.getOption(t).flatten,
              Parallax.milliarcseconds.reverseGet(130.23).some
            )
            // radial velocity
            assertEquals(
              Target.radialVelocity.getOption(t).flatten,
              RadialVelocity(-20.60.withUnit[KilometersPerSecond])
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }

  test("parse simbad named queries with sloan magnitudes") {
    // From http://simbad.u-strasbg.fr/simbad/sim-id?Ident=2MFGC6625&output.format=VOTable
    val xmlFile = "/simbad-2MFGC6625.xml"
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // id and search name
            assertEquals(t.name, refineMV[NonEmpty]("2MFGC6625"))
            assertEquals(t.tracking.catalogId,
                         CatalogId(CatalogName.Simbad, refineMV[NonEmpty]("2MFGC 6625")).some
            )
            // base coordinates
            assertEquals(
              Target.baseRA.getOption(t),
              RightAscension.fromHourAngle
                .get(HourAngle.fromHMS(8, 23, 54, 966, 933))
                .some
            )
            assertEquals(
              Target.baseDec.getOption(t),
              Declination
                .fromAngleWithCarry(Angle.fromDMS(28, 6, 21, 605, 409))
                ._1
                .some
            )
            // proper velocity
            assertEquals(Target.properMotion.getOption(t), none)
            // radial velocity
            assertEquals(
              Target.radialVelocity.getOption(t).flatten,
              RadialVelocity(13822.withUnit[KilometersPerSecond])
            )
            // parallax
            assertEquals(
              Target.parallax.getOption(t).flatten,
              none
            )
            // magnitudes
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanU).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(16.284),
                        MagnitudeBand.SloanU,
                        MagnitudeValue.fromDouble(0.007)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanG).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(15.728),
                        MagnitudeBand.SloanG,
                        MagnitudeValue.fromDouble(0.003)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanR).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(15.986),
                        MagnitudeBand.SloanR,
                        MagnitudeValue.fromDouble(0.004)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanI).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(15.603),
                        MagnitudeBand.SloanI,
                        MagnitudeValue.fromDouble(0.004)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanZ).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(15.682),
                        MagnitudeBand.SloanZ,
                        MagnitudeValue.fromDouble(0.008)
              ).some
            )
            // angular size
            assertEquals(
              t.angularSize,
              AngularSize(Angle.fromDMS(0, 0, 35, 400, 0), Angle.fromDMS(0, 0, 6, 359, 999)).some
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }
  test("parse simbad named queries with mixed magnitudes") {
    // From http://simbad.u-strasbg.fr/simbad/sim-id?Ident=2SLAQ%20J000008.13%2B001634.6&output.format=VOTable
    val xmlFile = "/simbad-J000008.13.xml"
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // id and search name
            assertEquals(t.name, refineMV[NonEmpty]("2SLAQ J000008.13+001634.6"))
            assertEquals(
              t.tracking.catalogId,
              CatalogId(CatalogName.Simbad, "2SLAQ J000008.13+001634.6")
            )
            // base coordinates
            assertEquals(
              Target.baseRA.getOption(t),
              RightAscension.fromHourAngle
                .get(HourAngle.fromHMS(0, 0, 8, 135, 999))
                .some
            )
            assertEquals(
              Target.baseDec.getOption(t),
              Declination
                .fromAngleWithCarry(Angle.fromDMS(0, 16, 34, 690, 799))
                ._1
                .some
            )
            // proper velocity
            assertEquals(Target.properMotion.getOption(t), none)
            // radial velocity
            assertEquals(
              Target.radialVelocity.getOption(t).flatten,
              RadialVelocity(233509.withUnit[KilometersPerSecond])
            )
            // parallax
            assertEquals(Target.parallax.getOption(t).flatten, none)
            // magnitudes
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.B).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(20.35), MagnitudeBand.B).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.V).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(20.03), MagnitudeBand.V).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.V).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(20.03), MagnitudeBand.V).some
            )
            // Bands J, H and K for this target have no standard magnitude system
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.J).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.399),
                        MagnitudeBand.J,
                        MagnitudeValue.fromDouble(0.073).some,
                        MagnitudeSystem.AB
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.H).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.416),
                        MagnitudeBand.H,
                        MagnitudeValue.fromDouble(0.137).some,
                        MagnitudeSystem.AB
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.K).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.176),
                        MagnitudeBand.K,
                        MagnitudeValue.fromDouble(0.115).some,
                        MagnitudeSystem.AB
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanU).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(20.233),
                        MagnitudeBand.SloanU,
                        MagnitudeValue.fromDouble(0.054)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanG).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(20.201),
                        MagnitudeBand.SloanG,
                        MagnitudeValue.fromDouble(0.021)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanR).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.929),
                        MagnitudeBand.SloanR,
                        MagnitudeValue.fromDouble(0.021)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanI).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.472),
                        MagnitudeBand.SloanI,
                        MagnitudeValue.fromDouble(0.023)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.SloanZ).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(19.191),
                        MagnitudeBand.SloanZ,
                        MagnitudeValue.fromDouble(0.068)
              ).some
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }
  test("force negative parallax values to zero") {
    // From http://simbad.u-strasbg.fr/simbad/sim-id?output.format=VOTable&Ident=HIP43018
    val xmlFile = "/simbad_hip43018.xml"

    val file = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // parallax
            assertEquals(
              Target.parallax.getOption(t).flatten,
              Parallax.Zero.some
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }

  test("parse simbad with a not-found name") {
    val xmlFile = "/simbad-not-found.xml"
    // Simbad returns non-valid xml when an element is not found, we need to skip validation :S
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .last
        .map {
          case Some(_) => fail("Cannot parse values")
          case _       => assert(true)
        }
    }
  }
  test("parse simbad with an npe") {
    val xmlFile = "/simbad-npe.xml"
    // Simbad returns non-valid xml when there is an internal error like an NPE
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .last
        .map {
          case Some(_) => fail("Cannot parse values")
          case _       => assert(true)
        }
    }
  }

  test("support simbad repeated magnitude entries and angular size") {
    val xmlFile = "/simbad-ngc-2438.xml"
    // Simbad returns an xml with multiple measurements of the same band, use only the first one
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // id and search name
            assertEquals(t.name, refineMV[NonEmpty]("NGC 2438"))
            assertEquals(t.tracking.catalogId, CatalogId(CatalogName.Simbad, "NGC  2438"))
            assert(t.tracking.properMotion.isEmpty)
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.J).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(17.02),
                        MagnitudeBand.J,
                        MagnitudeValue.fromDouble(0.15).some,
                        MagnitudeSystem.Vega
              ).some
            )
            assertEquals(
              t.angularSize,
              AngularSize(Angle.fromDMS(0, 1, 10, 380, 0), Angle.fromDMS(0, 1, 10, 380, 0)).some
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }

  test("parse xml with missing pm component") {
    // Taken from the url below and manually edited to remove PM RA
    // From http://simbad.u-strasbg.fr/simbad/sim-id?Ident=Vega&output.format=VOTable
    val xmlFile = "/simbad-vega-partial-pm.xml"
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // proper motions
            assertEquals(
              Target.properMotionRA.getOption(t),
              ProperMotion.RA.Zero.some
            )
            assertEquals(
              Target.properMotionDec.getOption(t),
              ProperMotion.Dec(286230.withUnit[MicroArcSecondPerYear]).some
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }

  test("parse simbad wildcard queries with name") {
    // From https://simbad.u-strasbg.fr/simbad/sim-id?Ident=name+vega*&NbIdent=wild&output.format=VOTable
    val xmlFile = "/simbad-vega-name.xml"
    // The sample has only one row
    val file    = getClass().getResource(xmlFile)
    Resource.unit[IO].use { _ =>
      Files[IO]
        .readAll(Path(file.getPath()))
        .through(text.utf8.decode)
        .through(targets(CatalogName.Simbad))
        .compile
        .lastOrError
        .map {
          case Validated.Valid(t)   =>
            // id and search name
            assertEquals(t.name, refineMV[NonEmpty]("Vega"))
            assertEquals(t.tracking.catalogId,
                         CatalogId(CatalogName.Simbad, refineMV[NonEmpty]("* alf Lyr")).some
            )
            // base coordinates
            assertEquals(
              Target.baseRA.getOption(t),
              RightAscension.fromDoubleDegrees(279.23473479).some
            )
            assertEquals(
              Target.baseDec.getOption(t),
              Declination.fromDoubleDegrees(38.78368896)
            )
            // proper motions
            assertEquals(
              Target.properMotionRA.getOption(t),
              ProperMotion.RA(200939.withUnit[MicroArcSecondPerYear]).some
            )
            assertEquals(
              Target.properMotionDec.getOption(t),
              ProperMotion.Dec(286230.withUnit[MicroArcSecondPerYear]).some
            )
            // magnitudes
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.U).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.U).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.B).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.B).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.V).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.03), MagnitudeBand.V).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.R).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.07), MagnitudeBand.R).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.I).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.10), MagnitudeBand.I).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.J).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(-0.177),
                        MagnitudeBand.J,
                        MagnitudeValue.fromDouble(0.206)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.H).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(-0.029),
                        MagnitudeBand.H,
                        MagnitudeValue.fromDouble(0.146)
              ).some
            )
            assertEquals(
              Target.magnitudeIn(MagnitudeBand.K).headOption(t),
              Magnitude(MagnitudeValue.fromDouble(0.129),
                        MagnitudeBand.K,
                        MagnitudeValue.fromDouble(0.186)
              ).some
            )
            // parallax
            assertEquals(
              Target.parallax.getOption(t).flatten,
              Parallax.milliarcseconds.reverseGet(130.23).some
            )
            // radial velocity
            assertEquals(
              Target.radialVelocity.getOption(t).flatten,
              RadialVelocity(-20.60.withUnit[KilometersPerSecond])
            )
          case Validated.Invalid(_) => fail(s"VOTable xml $xmlFile cannot be parsed")
        }
    }
  }
}
