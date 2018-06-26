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

import uk.gov.hmrc.models.{ServiceDetails, Subscription}
import uk.gov.hmrc.play.test.UnitSpec

class SubscriptionSpec extends UnitSpec {

   "Subscription" should {

     "fail validation if an empty serviceName is provided" in {

       try {
         Subscription("", "http://api-publisher.example.com/publish")
         fail("IllegalArgumentException was expected but not thrown")
       } catch {
         case e: IllegalArgumentException => e.getMessage shouldBe "requirement failed: serviceName is required"
       }

     }

     "fail validation if an empty callbackUrl is provided" in {

       try {
         Subscription("API-Publisher", "")
         fail("IllegalArgumentException was expected but not thrown")
       } catch {
         case e: IllegalArgumentException => e.getMessage shouldBe "requirement failed: callbackUrl is required"
       }

     }

   }

  "isCriteriaSatisfied" should {

    "return true if the subscription has no criteria specified" in {
      Subscription("service-name", "http://localhost:1234", None).isCriteriaSatisfied(ServiceDetails("service-name", "http://localhost:1234", Some(Map("key1" -> "value1"))))
    }

    "return true if the subscription has empty criteria specified" in {
      Subscription("service-name", "http://localhost:1234", Some(Map())).isCriteriaSatisfied(ServiceDetails("service-name", "http://localhost:1234", Some(Map("key1" -> "value1"))))
    }

    "return true if the subscription criteria is the same as the service details metadata" in {
      Subscription("service-name", "http://localhost:1234", Some(Map("key1" -> "value1"))).isCriteriaSatisfied(ServiceDetails("service-name", "http://localhost:1234", Some(Map("key1" -> "value1"))))
    }

    "return true if the subscription criteria is a subset of the service details metadata" in {
      Subscription("service-name", "http://localhost:1234", Some(Map("key1" -> "value1"))).isCriteriaSatisfied(ServiceDetails("service-name", "http://localhost:1234", Some(Map("key1" -> "value1", "key2" -> "value2"))))
    }

    "return false if the subscription criteria contains additional criteria that is not contained in the service details metadata" in {
      Subscription("service-name", "http://localhost:1234", Some(Map("key3" -> "value3"))).isCriteriaSatisfied(ServiceDetails("service-name", "http://localhost:1234", Some(Map("key1" -> "value1", "key2" -> "value2"))))
    }

  }

 }
