package core.services

import core.models._
import cats.data.NonEmptyList

trait BookRepository[F[_]] {
  def save(book: Book): F[Unit]
  def findById(id: Int): F[Book]
  def delete(id: Int): F[Unit]
  def findByCategory(category: String): F[NonEmptyList[Book]]
}
