package infra.db

import core.models._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

class BookDB(db: Database)(implicit ec: ExecutionContext) {

  private val books = TableQuery[BookTable]

  // Create: 書籍を追加
  def insertBook(book: Book): Future[UUID] = {
    val id = UUID.randomUUID()
    val action = books += (
      id,
      book.title,
      book.author,
      book.bibliographicIdentifier.map(_.toString),
      book.bibliographicIdentifier.map(_.getClass.getSimpleName),
      book.publishedYear,
      book.description,
      book.storageLocation,
      book.categories.mkString(",")
    )
    db.run(action).map(_ => id)
  }

  // Read: 全ての書籍を取得
  def selectAllBooks(): Future[Seq[Book]] = {
    db.run(books.result).map(_.map(toBook))
  }

  // Read: 指定されたIDの書籍を取得
  def selectBookById(id: UUID): Future[Option[Book]] = {
    db.run(books.filter(_.id === id).result.headOption).map(_.map(toBook))
  }

  // Update: 指定されたIDの書籍情報を更新
  def updateBook(id: UUID, book: Book): Future[Boolean] = {
    val updateAction = books
      .filter(_.id === id)
      .map(b => (b.title, b.author, b.description,b.publishedYear,b.storageLocation,b.categories))
      .update((book.title, book.author, book.description,book.publishedYear,book.storageLocation,book.categories.mkString(",")))
    db.run(updateAction).map(_ > 0)
  }

  // Delete: 指定されたIDの書籍を削除
  def deleteBookById(id: UUID): Future[Boolean] = {
    db.run(books.filter(_.id === id).delete).map(_ > 0)
  }

  private def toBook(row: (UUID, String, String, Option[String], Option[String], Option[Int], Option[String], Option[String], String)): Book = {
    val (id, title, author, identifier, identifierType, publishedYear, description, storageLocation, categories) = row
    Book(
      id,
      title,
      author,
      identifier.map(Identifier.fromString),
      publishedYear,
      description,
      storageLocation,
      categories.split(",").toSet
    )
  }
}

