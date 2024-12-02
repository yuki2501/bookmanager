package infra.db

import core.models._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

class CategoryDB(db: Database)(implicit ec: ExecutionContext) {

  private val categories = TableQuery[CategoryTable]

  // Create: カテゴリの追加
  def insertCategory(id: UUID, name: String): Future[Unit] = {
    db.run(categories += (id, name)).map(_ => ())
  }

  // Read: 全てのカテゴリを取得
  def selectAllCategories(): Future[Seq[(UUID, String)]] = {
    db.run(categories.result)
  }

  // Read: 特定のカテゴリを取得
  def selectCategoryById(id: UUID): Future[Option[(UUID, String)]] = {
    db.run(categories.filter(_.id === id).result.headOption)
  }

  // Update: カテゴリ名の更新
  def updateCategoryName(id: UUID, newName: String): Future[Boolean] = {
    db.run(categories.filter(_.id === id).map(_.name).update(newName)).map(_ > 0)
  }

  // Delete: カテゴリの削除
  def deleteCategoryById(id: UUID): Future[Boolean] = {
    db.run(categories.filter(_.id === id).delete).map(_ > 0)
  }
}

