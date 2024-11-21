package application

import core.models._
import core.services._
import infra.fetchers._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.server.Router
import cats.effect._
import io.circe.generic.auto._

class BookRoutes(
    isbnFetcher: OpenBDFetcher,
    isdnFetcher: ISDNMetadataFetcher
) {

  // エンドポイント定義
  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    // ISBN用エンドポイント
    case GET -> Root / "books" / "isbn" / isbnValue =>
      fetchBook(ByIdentifier(Identifier.ISBN(isbnValue)), isbnFetcher)

    // ISDN用エンドポイント
    case GET -> Root / "books" / "isdn" / isdnValue =>
      fetchBook(ByIdentifier(Identifier.ISDN(isdnValue)), isdnFetcher)
  }

  // 書籍データを取得してレスポンスを返す共通メソッド
  private def fetchBook(
      identifier: ByIdentifier,
      fetcher: BookMetadataFetcher[IO, ByIdentifier]
  ): IO[Response[IO]] = {
    fetcher.fetch(identifier).flatMap { books =>
      Ok(books.toList) // 正常系レスポンス
    }.handleErrorWith { error =>
      BadRequest(error.getMessage) // エラー時レスポンス
    }
  }

  // ルートを返す
  def httpRoutes: HttpRoutes[IO] = Router("/api" -> routes)
}

