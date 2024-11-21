package application

import core.models._
import core.services._
import cats.data._
import cats.effect._

// FetchBookUseCaseクラス定義
class FetchBookUseCase(
  isbnFetcher: BookMetadataFetcher[IO, ByIdentifier],
  isdnFetcher: BookMetadataFetcher[IO, ByIdentifier]
) {

  def fetch(identifier: ByIdentifier): IO[List[Book]] = identifier match {
        case ByIdentifier(Identifier.ISBN(x)) => isbnFetcher.fetch(ByIdentifier(Identifier.ISBN(x)))
        case ByIdentifier(Identifier.ISDN(x)) => isdnFetcher.fetch(ByIdentifier(Identifier.ISBN(x)))
    case _ => IO.raiseError(new RuntimeException("Unsupported identifier type"))
  }
}

