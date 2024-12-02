package core.models

sealed trait Identifier
object Identifier {
  // 各種識別子のケースクラス
  case class ISBN(value: String) extends Identifier
  case class ISDN(value: String) extends Identifier
  case class ISSN(value: String) extends Identifier
  case class Unknown(value: String) extends Identifier
  // 識別子を検証して適切な型に変換
  def fromString(value: String): Identifier = {
    if (value.matches("isbn_\\d{3}-\\d-\\d{6}-\\d{2}-\\d")) ISBN(value)
  else if (value.matches("isdn_\\d{3}-\\d-\\d{6}-\\d{2}-\\d$")) ISDN(value)
  else if (value.matches("issn_\\d{4}-\\d{4}\\d")) ISSN(value)
    else Unknown(value)
  }

  def value(v:Identifier):String = {
    v match{
      case ISBN(w) => w
      case ISDN(x) => x
      case ISSN(y) => y
      case Unknown(z) => z
    }

  }
}

