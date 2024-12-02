package application

import core.models._
import infra.fetchers._
import infra.db.BookDB
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.server.Router
import cats.effect._
import io.circe.generic.auto._
import java.util.UUID
import core.services._

class BookRoutes(
    isbnFetcher: OpenBDFetcher,
    isdnFetcher: ISDNMetadataFetcher,
    issnFetcher: ISSNMetadataFetcher,
    bookUseCase: BookUseCase
) {

  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    // ISBN用エンドポイント
    case GET -> Root / "books" / "isbn" / isbnValue =>
      fetchBook(ByIdentifier(Identifier.ISBN(isbnValue)), isbnFetcher)

    // ISDN用エンドポイント
    case GET -> Root / "books" / "isdn" / isdnValue =>
      fetchBook(ByIdentifier(Identifier.ISDN(isdnValue)), isdnFetcher)

    // ISSN用エンドポイント
    case GET -> Root / "books" / "issn" / issnValue =>
      fetchBook(ByIdentifier(Identifier.ISSN(issnValue)), issnFetcher)

    // Create: 新しい書籍を追加
    case req @ POST -> Root / "books" =>
      req.as[Book].flatMap { book =>
        IO.fromFuture(IO(bookUseCase.createBook(book))).flatMap { id =>
          Created(Map("id" -> id.toString))
        }.handleErrorWith { error =>
          BadRequest(error.getMessage)
        }
      }

    // Read: 全ての書籍を取得
    case GET -> Root / "books" =>
      IO.fromFuture(IO(bookUseCase.getAllBooks())).flatMap { books =>
        Ok(books)
      }.handleErrorWith { error =>
        BadRequest(error.getMessage)
      }

    // Read: 指定されたIDの書籍を取得
    case GET -> Root / "books" / UUIDVar(id) =>
      IO.fromFuture(IO(bookUseCase.getBookById(id))).flatMap {
        case Some(book) => Ok(book)
        case None       => NotFound(s"Book with id $id not found")
      }

    // Update: 指定されたIDの書籍情報を更新
    case req @ PUT -> Root / "books" / UUIDVar(id) =>
      req.as[Book].flatMap { book =>
        IO.fromFuture(IO(bookUseCase.updateBook(id, book))).flatMap {
          case true  => NoContent()
          case false => NotFound(s"Book with id $id not found")
        }
      }

    // Delete: 指定されたIDの書籍を削除
    case DELETE -> Root / "books" / UUIDVar(id) =>
      IO.fromFuture(IO(bookUseCase.deleteBook(id))).flatMap {
        case true  => NoContent()
        case false => NotFound(s"Book with id $id not found")
      }
  }

  private def fetchBook(
      identifier: ByIdentifier,
      fetcher: BookMetadataFetcher[IO, ByIdentifier]
  ): IO[Response[IO]] = {
    fetcher.fetch(identifier).flatMap { books =>
      Ok(books.toList)
    }.handleErrorWith { error =>
      BadRequest(error.getMessage)
    }
  }

  def httpRoutes: HttpRoutes[IO] = Router("/api" -> routes)
}

