package application

import application.CategoryUseCase
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import cats.effect.IO
import io.circe.generic.auto._
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class CategoryRoutes(categoryService: CategoryUseCase) {

  private val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // カテゴリの作成
    case req @ POST -> Root / "categories" =>
      req.as[Map[String, String]].flatMap { body =>
        body.get("name") match {
          case Some(name) =>
            IO.fromFuture(IO(categoryService.createCategory(name))).flatMap { id =>
              Created(Map("id" -> id.toString))
            }
          case None =>
            BadRequest("Missing 'name' field")
        }
      }

    // カテゴリ一覧の取得
    case GET -> Root / "categories" =>
      IO.fromFuture(IO(categoryService.getAllCategories())).flatMap { categories =>
        Ok(categories)
      }

    // 特定カテゴリの取得
    case GET -> Root / "categories" / UUIDVar(id) =>
      IO.fromFuture(IO(categoryService.getCategoryById(id))).flatMap {
        case Some(category) => Ok(category)
        case None           => NotFound(s"Category with id $id not found")
      }

    // カテゴリの更新
    case req @ PUT -> Root / "categories" / UUIDVar(id) =>
      req.as[Map[String, String]].flatMap { body =>
        body.get("name") match {
          case Some(newName) =>
            IO.fromFuture(IO(categoryService.updateCategory(id, newName))).flatMap {
              case true  => NoContent()
              case false => NotFound(s"Category with id $id not found")
            }
          case None =>
            BadRequest("Missing 'name' field")
        }
      }

    // カテゴリの削除
    case DELETE -> Root / "categories" / UUIDVar(id) =>
      IO.fromFuture(IO(categoryService.deleteCategory(id))).flatMap {
        case true  => NoContent()
        case false => NotFound(s"Category with id $id not found")
      }
  }

  def httpRoutes: HttpRoutes[IO] = routes
}

