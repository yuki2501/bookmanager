package core.services

import core.models._

trait MetadataQuery

case class ByIdentifier(identifier: Identifier) extends MetadataQuery
case class ByKeywords(keywords: String) extends MetadataQuery
case class ByPublisher(publisher: String) extends MetadataQuery

