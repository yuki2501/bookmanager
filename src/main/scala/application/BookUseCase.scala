package application

import infra.db.BookDB
import core.models._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

class BookUseCase(bookDB: BookDB)(implicit ec: ExecutionContext) {

  // 書籍の作成
  def createBook(book: Book): Future[UUID] = {
    bookDB.insertBook(book)
  }

  // 全ての書籍を取得
  def getAllBooks(): Future[Seq[Book]] = {
    bookDB.selectAllBooks()
  }

  // 特定の書籍を取得
  def getBookById(id: UUID): Future[Option[Book]] = {
    bookDB.selectBookById(id)
  }

  // 書籍情報の更新
  def updateBook(id: UUID, book: Book): Future[Boolean] = {
    bookDB.updateBook(id, book)
  }

  // 書籍の削除
  def deleteBook(id: UUID): Future[Boolean] = {
    bookDB.deleteBookById(id)
  }
}

