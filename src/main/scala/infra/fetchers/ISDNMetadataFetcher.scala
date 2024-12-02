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

  def parseBooks(xmlData: Elem, identifier: Identifier): List[Book] = {
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
      id = java.util.UUID.randomUUID(), // IDは生成時にDB側で付与されると仮定
      title = title,
      author = if (author.isEmpty) "Unknown" else author,
      bibliographicIdentifier = Some(identifier),
      publishedYear = Some(publishedYear),
      description = description,
      storageLocation = None, // 必要に応じて変更
      categories = categories
    )
  }

  //TODO: Remove this test
  val mockXmlString = """
  <isdn xsi:schemaLocation="https://isdn.jp/schemas/0.1 https://isdn.jp/schemas/0.1/isdn.xsd">
  <item key="2784506208037">
  <disp-isdn>ISDN278-4-506208-03-7</disp-isdn>
  <region>日本</region>
  <class>オリジナル</class>
  <type>同人誌</type>
  <rating_gender>区別なし</rating_gender>
  <rating_age>一般</rating_age>
  <product-name>カモガワGブックスVol.3 〈未来の文学〉完結記念号</product-name>
  <product-yomi>かもがわじーぶっくすぼりゅーむすりー みらいのぶんがくかんけつきねんごう</product-yomi>
  <publisher-code>86672282</publisher-code>
  <publisher-name>カモガワ編集室</publisher-name>
  <publisher-yomi>かもがわへんしゅうしつ</publisher-yomi>
  <issue-date>2021-11-23</issue-date>
  <genre-code>600</genre-code>
  <genre-name>評論・情報</genre-name>
  <genre-user/>
  <c-code>C0098</c-code>
  <author>一般</author>
  <shape>単行本</shape>
  <contents>外国文学、その他</contents>
  <price>1000</price>
  <price-unit>JPY</price-unit>
  <barcode2>2920098010003</barcode2>
  <product-comment/>
  <product-style>オンデマンド印刷・中綴じ</product-style>
  <product-size>A5判</product-size>
  <product-capacity>166</product-capacity>
  <product-capacity-unit>ページ</product-capacity-unit>
  <sample-image-uri/>
  <external-link>
  <title>BOOTH</title>
  <uri>https://hanfpen.booth.pm/</uri>
  </external-link>
  </item>
  </isdn>
"""
  assert(parseBooks(secureXML.loadString(mockXmlString),Identifier.ISDN("2784506208037")).head.title == "カモガワGブックスVol.3 〈未来の文学〉完結記念号")
  assert(parseBooks(secureXML.loadString(mockXmlString),Identifier.ISDN("2784506208037")).head.author == "一般")
  assert(parseBooks(secureXML.loadString(mockXmlString),Identifier.ISDN("2784506208037")).head.publishedYear == Some(2021))

}

