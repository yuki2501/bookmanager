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

class BookRoutes(fetcher: OpenBDFetcher) {

  // エンドポイント定義
  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "books" / "isbn" / isbnValue =>
      // ISBNを識別子に変換
          // Fetcherを呼び出して書籍データを取得
          fetcher.fetch(ByIdentifier(Identifier.ISBN(isbnValue))).flatMap { books =>
            Ok(books.toList) // 正常系レスポンス
          }.handleErrorWith { error =>
            BadRequest(error.getMessage) // エラー時レスポンス
          }
      }

  // ルートを返す
  def httpRoutes: HttpRoutes[IO] = Router("/api" -> routes)
}

