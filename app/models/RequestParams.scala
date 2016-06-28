package models

import play.api.libs.json._

object request_params {

  case class MyNel[A](head: A, tail: List[A]) {
    def toList = head :: tail
  }

  sealed trait MetricType
  case object Ranges extends MetricType
  case object Rising extends MetricType
  case object Falling extends MetricType
  val metricTypeValues = List(Ranges, Rising, Falling)

  def parseMetricType(str: String): Option[MetricType] = {
    metricTypeValues.find(_.toString.toUpperCase == str)
  }


  type StockSign = String
  case class Params(
    stocks: MyNel[StockSign],
    metrics: Set[MetricType]
  )

  implicit val metricTypeReads = new Reads[MetricType] {
    def reads(js: JsValue): JsResult[MetricType] = js match {
      case JsString(str) => parseMetricType(str) match {
        case Some(v) => JsSuccess(v)
        case _       => JsError("invalid metric type")
      }
      case _             => JsError("invalid metric type")
    }
  }

  implicit def myNelReads[A](implicit r: Reads[A]) = new Reads[MyNel[A]] {
    def reads(js: JsValue): JsResult[MyNel[A]] = js match {
      case JsArray(values) => values.toList match {
        case head :: tail => {
          Json.fromJson[List[A]](js).map(values => MyNel(values.head, values.tail))
        }
        case Nil => JsError("NonEmptyList must have at least one element")
      }
      case _     => JsError("invalid NonEmptyList")
    }
  }

  implicit val paramsReads = Json.reads[Params]



  /*
  {
    "stocks": ["AAPL"], // au moins une valeur
    "metrics": [ // au moins un de ces trois choix
      "RANGES",
      "RISING",
      "FALLING"
    ]
  }
  */
}
