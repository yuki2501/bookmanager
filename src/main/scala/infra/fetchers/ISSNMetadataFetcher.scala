package infra.fetchers

import core.models._
import core.services._
import org.http4s._
import org.http4s.client._
import cats.effect.IO
import io.circe._
import io.circe.parser._

class ISSNMetadataFetcher(httpClient: Client[IO]) extends BookMetadataFetcher[IO, ByIdentifier] {
  private val baseUrl = "https://portal.issn.org/resource/ISSN"

  override def fetch(identifier: ByIdentifier): IO[List[Book]] = {
    identifier.identifier match {
      case Identifier.ISSN(issn) =>
        val requestUri = Uri.unsafeFromString(s"$baseUrl/$issn?format=json")

        // HTTPリクエストを作成
        val request = Request[IO](
          method = Method.GET,
          uri = requestUri
        )

        for {
          // HTTPリクエストを実行してレスポンスを取得
          response <- httpClient.expect[String](request)

          // レスポンスを解析
          books <- processResponse(response, identifier.identifier) match {
            case Right(b) => IO.pure(List(b))
            case Left(err) => IO.raiseError(new RuntimeException(err))
          }
        } yield books

      case _ =>
        IO.raiseError(new IllegalArgumentException("Invalid identifier type for ISSNMetadataFetcher"))
    }
  }
// decodeのエラーが出たのでとりあえずhcursorを使ってみる
  private def processResponse(
      response: String,
      identifier: Identifier
  ): Either[String, Book] = {
    parse(response).flatMap { json =>
      val cursor = json.hcursor

      // `@graph`フィールドからデータを取得
      val graphItems = cursor.downField("@graph").as[List[Json]]
      graphItems.flatMap { items =>
        // `mainTitle`フィールドを持つ最初のアイテムを探す
        items.find(_.hcursor.downField("mainTitle").focus.isDefined) match {
          case Some(item) =>
            val itemCursor = item.hcursor
            val title = itemCursor.downField("mainTitle").as[List[String]].toOption.flatMap(_.headOption).getOrElse("No Title")
            val publisher = itemCursor.downField("publisher").as[String].getOrElse("Unknown Publisher")

            Right(
              Book(
                id = java.util.UUID.randomUUID(),
                title = title,
                author = publisher,
                bibliographicIdentifier = Some(identifier),
                publishedYear = None,
                description = None,
                storageLocation = None,
                categories = Set.empty
              )
            )
          case None =>
            Left("No valid graph item found in the ISSN response.")
        }
      }
    }.left.map(_.toString())
  }

  private def extractYear(publication: Option[String]): Option[Int] = {
    publication.flatMap { pub =>
      """\d{4}""".r.findFirstIn(pub).map(_.toInt)
    }
  }
}

