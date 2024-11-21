package core.models


opaque type StorageLocation = String

// Book 型の定義
case class Book(
  id: Int,
  title: String,
  author: String,
  bibliographicIdentifier: Option[Identifier],
  publishedYear: Int,
  description: Option[String],
  storageLocation: Option[String],
  categories: Set[String]
)
