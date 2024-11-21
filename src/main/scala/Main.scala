package main

import application.BookRoutes
import infra.fetchers.OpenBDFetcher
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import cats.effect._
import scala.concurrent.ExecutionContext.global
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.loggerFactoryforSync

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val clientResource = BlazeClientBuilder[IO](global).resource

    clientResource.use { httpClient =>
      val fetcher = new OpenBDFetcher(httpClient)
      val bookRoutes = new BookRoutes(fetcher)

      BlazeServerBuilder[IO](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(bookRoutes.httpRoutes.orNotFound)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }
  }
}

