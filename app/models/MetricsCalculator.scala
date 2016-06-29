package models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import play.api.libs.ws._
import play.api.libs.json._

import models.request_params._
import models.api_dto._


object metrics_calculator {
  sealed trait MetricsResult
  case class ResourcesList(values: List[ResourceWrapper]) extends MetricsResult
  case class ResourcesMap(values: Map[String, List[ResourceWrapper]]) extends MetricsResult

  implicit val metricsResultWrites = new Writes[MetricsResult] {
    def writes(mr: MetricsResult): JsValue = mr match {
      case ResourcesList(values) => Json.toJson(values)
      case ResourcesMap(values) => Json.toJson(values)
    }
  }

  def computeMetrics(metrics: Set[MetricType], data: List[ResourceWrapper]): List[MetricsResult] = {
    metrics.toList.map({
      case Rising  => ResourcesList(data.filter(_.resource.fields.change > 0))
      case Falling => ResourcesList(data.filter(_.resource.fields.change < 0))
      case Ranges  => ResourcesMap(???)
    })
  }

  def fetchAndComputeMetrics(params: Params)(ws: WSClient): Future[List[MetricsResult]] = {
    val rawData = fetchRawData(params.stocks)(ws)

    rawData.map(data => {
      computeMetrics(params.metrics, data)
    })
  }

  def parseResult(result: WSResponse): Try[List[ResourceWrapper]] = {
    val resourcesResult = for {
      json <- (result.json \ "list" \ "resources").toEither.right
      resources <- Json.fromJson[List[ResourceWrapper]](json).asEither.right
    } yield resources

    Try {
      resourcesResult match {
        case Left(_) => throw new Exception("remote api call failed")
        case Right(resources) => resources
      }
    }
  }

  def makeQueryUrl(stocks: MyNel[StockSign]): String = {
    s"http://finance.yahoo.com/webservice/v1/symbols/${stocks.toList.mkString(",")}/quote"
  }

  def fetchRawData(stocks: MyNel[StockSign])(ws: WSClient): Future[List[ResourceWrapper]] = {
    val request =
     ws.url(makeQueryUrl(stocks))
       .withQueryString(
         "format" -> "json",
         "view" -> "detail"
       )

    val response = request.get()

    response.flatMap(result => {
      Future.fromTry(parseResult(result))
    })
  }
}
