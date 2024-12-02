package infra.db

import core.models._
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

// Bookテーブルの定義
class BookTable(tag: Tag) extends Table[(UUID,String,String,Option[String],Option[String],Option[Int],Option[String],Option[String],String)](tag, "books") {
  // カラム定義
  def id = column[UUID]("id", O.PrimaryKey)
  def title = column[String]("title")
  def author = column[String]("author")
  def bibliographicIdentifier = column[Option[String]]("identifier") // 識別子は文字列で保存
  def identifierType = column[Option[String]]("identifier_type")     // 識別子の種類 (ISBN, ISDNなど)
  def publishedYear = column[Option[Int]]("published_year")
  def description = column[Option[String]]("description")
  def storageLocation = column[Option[String]]("storage_location")
  def categories = column[String]("categories") // カンマ区切りで保存
  def * = (id,title, author, bibliographicIdentifier, identifierType, publishedYear, description, storageLocation, categories)
}
