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

import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.controllers.RegistrationController
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.play.microservice.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.services.RegistrationService

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with ScalaFutures with WithFakeApplication with MockitoSugar {

  trait Setup extends MicroserviceFilterSupport {
    implicit val hc = HeaderCarrier()

    DateTimeUtils.setCurrentMillisFixed(1444043407060L)
    val mockRegistrationService = mock[RegistrationService]

    implicit lazy val request = FakeRequest()

    val underTest = new RegistrationController(mockRegistrationService)
  }
  DateTimeUtils.setCurrentMillisFixed(DateTime.now().toDate.getTime)

  "register" should {

    "succeed with a 204 (no content) when payload is valid and service responds successfully" in new Setup {

      val body = """{ "serviceName": "Hello World", "serviceUrl": "http://hello-world.example.com", "metadata": { "third-party": "true" } }"""

      val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))

      when(mockRegistrationService.register(ArgumentMatchers.eq(serviceDetails))(any())).thenReturn(Future.successful(serviceDetails))

      val result = await(underTest.register()(request.withBody(Json.parse(body))))

      status(result) shouldBe 204

      verify(mockRegistrationService).register(ArgumentMatchers.eq(serviceDetails))(any())

    }

    "fail with a 422 (invalid request) when the json payload is invalid for the request" in new Setup {

      val body = """{ "invalid": "json" }"""

      val result = underTest.register()(request.withBody(Json.parse(body)))

      status(result) shouldBe 422

      verifyZeroInteractions(mockRegistrationService)

    }

    "fail with a 500 (internal server error) when the service throws an exception" in new Setup {

      val body = """{ "serviceName": "Hello World", "serviceUrl": "http://hello-world.example.com", "metadata": { "third-party": "true" } }"""

      val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))

      when(mockRegistrationService.register(ArgumentMatchers.eq(serviceDetails))(any()))
        .thenReturn(Future.failed(new RuntimeException("expected test exception")))

      val result = await(underTest.register()(request.withBody(Json.parse(body))))

      status(result) shouldBe 500
    }

  }

  "fetchService" should {

    "succeed with a 200 (ok) when payload is valid and service responds successfully" in new Setup {

      val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))

      when(mockRegistrationService.fetchService("Hello World")).thenReturn(Future.successful(Some(serviceDetails)))

      val result = await(underTest.fetchService("Hello World")(request))

      status(result) shouldBe 200

      bodyOf(result) shouldBe """{"serviceName":"Hello World","serviceUrl":"http://hello-world.example.com","metadata":{"third-party":"true"},"lastModified":1444043407060}"""

    }

    "fail with a 500 (internal server error) when the service throws an exception" in new Setup {

      val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))

      when(mockRegistrationService.fetchService("Hello World")).thenReturn(Future.failed(new RuntimeException("expected test exception")))

      val result = await(underTest.fetchService("Hello World")(request))

      status(result) shouldBe 500
    }

  }


}
