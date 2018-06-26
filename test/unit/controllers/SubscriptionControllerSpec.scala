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

package unit.controllers

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.controllers.SubscriptionController
import uk.gov.hmrc.models.Subscription
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.services.SubscriptionService

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with ScalaFutures with WithFakeApplication with MockitoSugar {

  trait Setup {

    val mockSubscriptionService = mock[SubscriptionService]

    implicit lazy val request = FakeRequest()

    val underTest = new SubscriptionController(mockSubscriptionService)
  }

  "subscribe" should {

    "succeed with a 204 (no content) when payload is valid and service responds successfully" in new Setup {

      val body = """{ "serviceName" : "API-Publisher", "callbackUrl": "http://api-publisher.example.com/publish" }"""

      val subscription = Subscription("API-Publisher", "http://api-publisher.example.com/publish")

      when(mockSubscriptionService.subscribe(subscription)).thenReturn(Future.successful(()))

      val result = await(underTest.subscribe()(request.withBody(Json.parse(body))))

      status(result) shouldBe 204

      verify(mockSubscriptionService).subscribe(subscription)

    }

    "fail with a 422 (invalid request) when the json payload is invalid for the request" in new Setup {

      val body = """{ "invalid": "json" }"""

      val result = await(underTest.subscribe()(request.withBody(Json.parse(body))))

      status(result) shouldBe 422

      verifyZeroInteractions(mockSubscriptionService)

    }

    "fail with a 500 (internal server error) when the service throws an exception" in new Setup {

      val body = """{ "serviceName" : "API-Publisher", "callbackUrl": "http://api-publisher.example.com/publish" }"""

      val subscription = Subscription("API-Publisher", "http://api-publisher.example.com/publish")

      when(mockSubscriptionService.subscribe(subscription)).thenReturn(Future.failed(new RuntimeException("expected test exception")))

      val result = await(underTest.subscribe()(request.withBody(Json.parse(body))))

      status(result) shouldBe 500
    }

  }

}
