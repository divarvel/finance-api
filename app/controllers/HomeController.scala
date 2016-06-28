package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.ws._
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import models.api_dto._
import models.request_params._
import models.metrics_calculator._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (ws: WSClient) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action.async { req =>
    val ojs = req.body.asJson

    ojs match {
      case Some(js) => {
        Json.fromJson[Params](js) match {
          case JsSuccess(params, _) => {
            val result = fetchAndComputeMetrics(params)(ws)
            result.map(metricsResults => {
              Ok(Json.toJson(metricsResults))
            })
          }
          case JsError(errors) => {
            Future.successful(BadRequest(errors.toString))
          }
        }
      }
      case None => Future.successful(BadRequest("expecting json"))
    }
  }

}
