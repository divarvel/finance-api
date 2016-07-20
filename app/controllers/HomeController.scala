package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.ws._
import play.api.libs.json._

import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport,Lang,Messages,MessagesApi}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import models.api_dto._
import models.request_params._
import models.metrics_calculator._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
  case class ParamsMoche(
    stocks: List[String],
    metrics: List[String]
  )

@Singleton
class HomeController @Inject() (ws: WSClient, messages: MessagesApi) extends Controller with I18nSupport {
  def messagesApi = messages

  def indexForm = Action { implicit request =>
    Ok(views.html.index(paramForm))
  }

  private def paramsFromMoche(pm: ParamsMoche): Option[Params] = pm.stocks match {
    case h :: t => Some(Params(MyNel(h, t), Set(Rising)))
    case Nil => None
  }


  val paramForm = Form(mapping(
    "stocks" -> list(text),
    "metrics" -> list(text)
  )(ParamsMoche.apply)(ParamsMoche.unapply))

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action.async { implicit req =>
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
      case None => {
        val paramsMoche = paramForm.bindFromRequest
        paramsMoche.fold(
          errors => {
            Future.successful(BadRequest(views.html.index(errors)))
          },
          pm => {
            paramsFromMoche(pm) match {
              case Some(params) => {
                val result = fetchAndComputeMetrics(Params(MyNel("AAPL", Nil), Set(Rising)))(ws)
                result.map(metricsResults => {
                  Ok(views.html.resultsPage(metricsResults))
                })
              }
              case None => Future.successful(BadRequest(views.html.index(paramForm.fill(pm).withGlobalError("paramètres invalides"))))
            }
          }
        )
      }
    }
  }

}
