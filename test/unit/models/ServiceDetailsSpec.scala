/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.models

import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.play.test.UnitSpec

class ServiceDetailsSpec extends UnitSpec {

  "ServiceDetails" should {

    "fail validation if an empty serviceName is provided" in {

      try {
        ServiceDetails("", "http://hello-world.example.com", Some(Map("third-party" -> "true")))
        fail("IllegalArgumentException was expected but not thrown")
      } catch {
        case e: IllegalArgumentException => e.getMessage shouldBe "requirement failed: serviceName is required"
      }

    }

    "fail validation if an empty serviceUrl is provided" in {

      try {
        ServiceDetails("Hello World", "", Some(Map("third-party" -> "true")))
        fail("IllegalArgumentException was expected but not thrown")
      } catch {
        case e: IllegalArgumentException => e.getMessage shouldBe "requirement failed: serviceUrl is required"
      }

    }

  }

}
