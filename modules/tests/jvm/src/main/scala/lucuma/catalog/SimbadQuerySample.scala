// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.catalog

import cats.implicits._
import cats.effect._
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3._
import sttp.capabilities.fs2.Fs2Streams
import fs2.text
import lucuma.core.enum.CatalogName

object SimbadQuerySample extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val blocker: Blocker =
      Blocker.liftExecutionContext(scala.concurrent.ExecutionContext.global)
    AsyncHttpClientFs2Backend
      .resource[IO](blocker)
      .use { backend =>
        val response = basicRequest
          .post(
            uri"http://simbad.u-strasbg.fr/simbad/sim-id?Ident=2SLAQ%20J000008.13%2B001634.6&output.format=VOTable"
          )
          .response(asStreamUnsafe(Fs2Streams[IO]))
          .send(backend)

        response
          .flatMap(
            _.body
              .traverse(
                _.through(text.utf8Decode)
                  .through(VoTableParser.targets(CatalogName.Simbad))
                  .compile
                  .lastOrError
              )
          )
          .flatMap {
            case Right(t) => IO(pprint.pprintln(t))
            case _        => IO.unit
          }
      }
      .as(ExitCode.Success)
  }
}
