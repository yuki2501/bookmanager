package infra.fetchers

import core.services._
import core.models._
import org.http4s._
import org.http4s.client._
import cats.effect.IO
import scala.xml.{XML, Elem}
import scala.xml.factory.XMLLoader
import javax.xml.parsers.SAXParserFactory
import cats.data.NonEmptyList

// https://xuwei-k.hatenablog.com/entry/20150323/1427077733
// なんかふつうのloadString使うのダメらしい
def secureXML: XMLLoader[Elem] = {
  val parserFactory = SAXParserFactory.newInstance()
  parserFactory.setNamespaceAware(false)
  parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
  parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
  val saxParser = parserFactory.newSAXParser()
  XML.withSAXParser(saxParser)
}

class ISDNMetadataFetcher(httpClient: Client[IO]) extends BookMetadataFetcher[IO, ByIdentifier] {

  override def fetch(query: ByIdentifier): IO[NonEmptyList[Book]] = {
    val url = s"https://isdn.jp/xml/${query.identifier}"

    for {
      // HTTPリクエストを送信
      response <- httpClient.expect[String](Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString(url)
      ))

      // XMLデータを解析
      xmlData = secureXML.loadString(response)

      // Bookデータを生成
      books = NonEmptyList(
        Book(
          id = 0,
          title = (xmlData \ "product-name").text,
          author = (xmlData \ "publisher-name").text,
          bibliographicIdentifier = Some(query.identifier),
          publishedYear = (xmlData \ "issue-date").text.take(4).toInt,
          description = (xmlData \ "product-comment").headOption.map(_.text),
          storageLocation = None,
          categories = (xmlData \ "genre-name").map(_.text).toSet
        ),List.empty
      )
    } yield books
  }
}

