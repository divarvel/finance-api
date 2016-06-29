import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

import org.scalatest._
import prop._

import models.metrics_calculator._
import models.request_params._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class UnitTest extends PlaySpec {
  "computeMetrics" should {
    "return an empty list if asked for no metrics" in {
      val result = computeMetrics(Set(), List())
      result mustBe Nil
    }
    "return empty results if no stock is given" in {
      val result = computeMetrics(Set(Rising), Nil)
      result mustBe List(ResourcesList(Nil))
    }
  }
}
