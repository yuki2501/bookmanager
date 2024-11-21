package core.services

import core.models._
import cats.data.NonEmptyList

trait BookMetadataFetcher[F[_], Q <: MetadataQuery] {
  def fetch(query: Q): F[NonEmptyList[Book]]
}
