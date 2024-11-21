package main

import application.BookRoutes
import infra.fetchers.{OpenBDFetcher, ISDNMetadataFetcher}
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
      for {
        // ロガーの初期化
        logger: Logger[IO] <- Slf4jLogger.create[IO]

        // 各フェッチャーのインスタンスを作成
        isbnFetcher = new OpenBDFetcher(httpClient)
        isdnFetcher = new ISDNMetadataFetcher(httpClient)

        // ルートの設定
        bookRoutes = new BookRoutes(isbnFetcher, isdnFetcher)

        // サーバーの起動
        _ <- logger.info("Starting HTTP server on http://localhost:8080")
        exitCode <- BlazeServerBuilder[IO](global)
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(bookRoutes.httpRoutes.orNotFound)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
        _ <- logger.info("HTTP server stopped")
      } yield exitCode
    }
  }
}

