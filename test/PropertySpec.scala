import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import org.scalatest._
import prop._


import models.request_params._
import models.metrics_calculator._
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class PropertySpec extends PlaySpec with GeneratorDrivenPropertyChecks {

  "MyNel" should {

    "keep the list length in toList" in  {
      forAll ("h", "t") { (h: String, t: List[String]) =>
        MyNel(h, t).toList.length mustBe (1 + t.length)
      }
    }

    "only produce lists with at least one element in toList" in  {
      forAll ("h", "t") { (h: String, t: List[String]) =>
        MyNel(h, t).toList.length must be >= 1
      }
    }

    "should have reversible ser/de" in  {
      forAll ("h", "t") { (h: String, t: List[String]) =>
        val nel = MyNel(h, t)
        val js = Json.toJson(nel.toList)
        val res = Json.fromJson[MyNel[String]](js)
        res.asOpt mustBe Some(nel)
      }
    }
  }

  "makeQueryUrl" should {
    "always produce a legal url" in  {
      forAll ("h", "t") { (h: String, t: List[String]) =>
        val stocks = MyNel(h, t)
        val result = makeQueryUrl(stocks)

        val url = new java.net.URL(result)
        url must not be null

      }
    }

  }
}
