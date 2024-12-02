package core.models

import java.util.UUID


opaque type StorageLocation = String

// Book 型の定義
case class Book(
  id: UUID,
  title: String,
  author: String,
  bibliographicIdentifier: Option[Identifier],
  publishedYear: Option[Int],
  description: Option[String],
  storageLocation: Option[String],
  categories: Set[String]
)
