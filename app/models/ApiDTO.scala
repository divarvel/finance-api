package models

import play.api.libs.json._

import java.time._

object api_dto {

  case class Fields(
    change: BigDecimal,
    chg_percent: BigDecimal,
    day_high: BigDecimal,
    day_low: BigDecimal,
    issuer_name: String,
    issuer_name_lang: String,
    name: String,
    price: BigDecimal,
    symbol: String,
    ts: String,
    `type`: String,
    utctime: String,
    volume: BigDecimal,
    year_high: BigDecimal,
    year_low: BigDecimal
  )

  case class Resource(
    classname: String,
    fields: Fields
  )

  case class ResourceWrapper(
    resource: Resource
  )

  implicit val fieldsFormat = Json.format[Fields]
  implicit val resourceFormat = Json.format[Resource]
  implicit val resourceWrapperFormat = Json.format[ResourceWrapper]
}
