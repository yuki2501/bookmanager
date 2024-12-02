package infra.db

import core.models._
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

// Categoryテーブルの定義
class CategoryTable(tag: Tag) extends Table[(UUID, String)](tag, "categories") {
  // カラム定義
  def id = column[UUID]("id", O.PrimaryKey)
  def name = column[String]("name") // カテゴリ名

  // テーブルマッピング
  def * = (id, name)
}

// 本とカテゴリの中間テーブル定義
class BookCategoryTable(tag: Tag) extends Table[(UUID, UUID)](tag, "book_categories") {
  def bookId = column[UUID]("book_id") // 本のID
  def categoryId = column[UUID]("category_id") // カテゴリのID

  // 複合主キーを定義（1つの本に同じカテゴリが重複しないようにする）
  def pk = primaryKey("pk_book_category", (bookId, categoryId))

  // 外部キー制約を定義
  def bookFk = foreignKey("fk_book", bookId, TableQuery[BookTable])(_.id)
  def categoryFk = foreignKey("fk_category", categoryId, TableQuery[CategoryTable])(_.id)

  // テーブルマッピング
  def * = (bookId, categoryId)
}

