package main

import application.{BookRoutes, CategoryRoutes}
import application.{CategoryUseCase, BookUseCase}
import infra.db.{CategoryDB, BookDB}
import infra.fetchers.{OpenBDFetcher, ISDNMetadataFetcher, ISSNMetadataFetcher}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import slick.jdbc.PostgresProfile.api._
import cats.effect._
import scala.concurrent.ExecutionContext
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import cats.MonoidK.ops.toAllMonoidKOps

object Main extends IOApp {

  // Slick用のExecutionContextを明示的に定義
  implicit val slickExecutionContext: ExecutionContext = ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] = {
    val clientResource = BlazeClientBuilder[IO](slickExecutionContext).resource
    val db = Database.forConfig("postgres") // DB設定

    clientResource.use { httpClient =>
      for {
        // ロガーの初期化
        logger: Logger[IO] <- Slf4jLogger.create[IO]

        // 各フェッチャーのインスタンスを作成
        isbnFetcher = new OpenBDFetcher(httpClient)
        isdnFetcher = new ISDNMetadataFetcher(httpClient)
        issnFetcher = new ISSNMetadataFetcher(httpClient)

        // Category 関連の DAO とサービスを作成
        categoryDB = new CategoryDB(db)
        categoryService = new CategoryUseCase(categoryDB)

        // ルートの設定
        bookDB = new BookDB(db)
        bookUseCase = new BookUseCase(bookDB) // Databaseを渡してBookUseCaseを作成
        bookRoutes = new BookRoutes(isbnFetcher, isdnFetcher, issnFetcher, bookUseCase)
        categoryRoutes = new CategoryRoutes(categoryService)

        // サーバーの起動
        _ <- logger.info("Starting HTTP server on http://localhost:8080")
        exitCode <- BlazeServerBuilder[IO](slickExecutionContext)
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp((bookRoutes.httpRoutes <+> categoryRoutes.httpRoutes).orNotFound)
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
        _ <- logger.info("HTTP server stopped")
      } yield exitCode
    }
  }
}

