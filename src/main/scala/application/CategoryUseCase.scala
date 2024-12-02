package application

import infra.db.CategoryDB
import core.models._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

class CategoryUseCase(categoryDB: CategoryDB)(implicit ec: ExecutionContext) {

  // カテゴリの作成
  def createCategory(name: String): Future[UUID] = {
    val id = UUID.randomUUID()
    categoryDB.insertCategory(id, name).map(_ => id)
  }

  // 全てのカテゴリを取得
  def getAllCategories(): Future[Seq[(UUID, String)]] = {
    categoryDB.selectAllCategories()
  }

  // 特定のカテゴリを取得
  def getCategoryById(id: UUID): Future[Option[(UUID, String)]] = {
    categoryDB.selectCategoryById(id)
  }

  // カテゴリの更新
  def updateCategory(id: UUID, newName: String): Future[Boolean] = {
    categoryDB.updateCategoryName(id, newName)
  }

  // カテゴリの削除
  def deleteCategory(id: UUID): Future[Boolean] = {
    categoryDB.deleteCategoryById(id)
  }
}

