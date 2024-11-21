package infra.fetchers

import core.services._
import core.models._
import org.http4s._
import org.http4s.client._
import cats.effect.IO
import scala.xml.{XML, Elem, NodeSeq}
import scala.xml.factory.XMLLoader
import javax.xml.parsers.SAXParserFactory
import cats.data.NonEmptyList

// 安全なXMLロードのための設定
def secureXML: XMLLoader[Elem] = {
  val parserFactory = SAXParserFactory.newInstance()
  parserFactory.setNamespaceAware(false)
  parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
  parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
  val saxParser = parserFactory.newSAXParser()
  XML.withSAXParser(saxParser)
}

class ISDNMetadataFetcher(httpClient: Client[IO]) extends BookMetadataFetcher[IO, ByIdentifier] {

  override def fetch(query: ByIdentifier): IO[List[Book]] = {
    val url = s"https://isdn.jp/xml/${Identifier.value(query.identifier)}"

    for {
      // HTTPリクエストを送信
      response <- httpClient.expect[String](Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(url)
      ))

      // XMLデータを解析
      xmlData = secureXML.loadString(response)

      // Bookデータを生成
      books = parseBooks(xmlData, query.identifier)
    } yield books
  }

  private def parseBooks(xmlData: Elem, identifier: Identifier): List[Book] = {
    val items = (xmlData \ "item")

    if (items.isEmpty) {
      List.empty
    } else {
          items.map(parseBook(_, identifier)).toList
    }
  }

  private def parseBook(item: NodeSeq, identifier: Identifier): Book = {
    val title = (item \ "product-name").text
    val author = (item \ "author").text
    val description = (item \ "product-comment").headOption.map(_.text)
    val categories = (item \ "genre-name").map(_.text).toSet
    val publishedYear = (item \ "issue-date").text.take(4).toIntOption.getOrElse(0)

    Book(
      id = 0, // IDは生成時にDB側で付与されると仮定
      title = title,
      author = if (author.isEmpty) "Unknown" else author,
      bibliographicIdentifier = Some(identifier),
      publishedYear = publishedYear,
      description = description,
      storageLocation = None, // 必要に応じて変更
      categories = categories
    )
  }
}

