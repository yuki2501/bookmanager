package infra.fetchers

import core.models._
import core.services._
import org.http4s._
import org.http4s.client._
import org.http4s.circe.CirceEntityDecoder._
import cats.effect.IO
import io.circe.generic.auto._
import cats.data.NonEmptyList

// OpenBD APIレスポンススキーマ
case class Summary(title:Option[String],author:Option[String])
case class OpenBDResponse(summary: Option[Summary])

class OpenBDFetcher(httpClient: Client[IO]) extends BookMetadataFetcher[IO, ByIdentifier] {
  private val baseUrl = "https://api.openbd.jp/v1/get"

  override def fetch(identifier: ByIdentifier): IO[List[Book]] = {
    val requestUri = Uri.unsafeFromString(s"$baseUrl?isbn=${Identifier.value(identifier.identifier)}")

    // HTTPリクエストを作成
    val request = Request[IO](
      method = Method.GET,
      uri = requestUri
    )

    for {
      // HTTPリクエストを実行
      response <- httpClient.expect[List[Option[OpenBDResponse]]](request)

      // レスポンスを解析して書籍データを生成
      book <- processResponse(response, identifier.identifier) match {
        case Right(b) => IO.pure(List(b))
        case Left(err) => IO.raiseError(new RuntimeException(err))
      }
    } yield book
  }

  private def processResponse(
      response: List[Option[OpenBDResponse]],
      identifier: Identifier
  ): Either[String, Book] = {
    response.headOption.flatten match {
      case Some(OpenBDResponse(Some(summary))) =>
        val title = summary.title
        val author = summary.author
        val language = None
        Right(
          Book(
            id = 0,
            title = title.getOrElse("No Title"),
            author = author.getOrElse("No Author"), // Author情報はスキーマにないため、必要に応じて追加
            bibliographicIdentifier = None,
            publishedYear = 0, // PublishedYearの取得処理を追加する場合はここに記載
            description = language.map(l => s"Language: $l"),
            storageLocation = None,
            categories = Set.empty
          )
        )
      case _ =>
        Left("No valid data found for the given ISBN.")
    }
  }
}

