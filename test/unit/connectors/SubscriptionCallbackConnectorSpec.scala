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

package unit.connectors

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.stubbing.Scenario
import org.joda.time.DateTimeUtils
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.config.WSHttp
import uk.gov.hmrc.connectors.SubscriptionCallbackConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SubscriptionCallbackConnectorSpec extends UnitSpec
  with WithFakeApplication with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val stubPort = sys.env.getOrElse("WIREMOCK", "22222").toInt
  val stubHost = "localhost"
  val wireMockUrl = s"http://$stubHost:$stubPort"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val timeout = Duration(5, TimeUnit.SECONDS)

  trait Setup {
    implicit val hc = HeaderCarrier()
    DateTimeUtils.setCurrentMillisFixed(1444043407060L)
    val underTest = new SubscriptionCallbackConnector(WSHttp())
  }

  override def beforeEach(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit = {
    WireMock.resetToDefault()
    wireMockServer.stop()
  }

  "issueCallback" should {

    "post the service details to the callback url" in new Setup {

      val callbackUrl = s"$wireMockUrl/publish"
      val serviceDetails = ServiceDetails("Hello World", "http://helloworld.example.com", Some(Map("third-party" -> "true")))

      stubFor(post(urlEqualTo("/publish"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo("""{"serviceName":"Hello World","serviceUrl":"http://helloworld.example.com","metadata":{"third-party":"true"},"lastModified":1444043407060}"""))
        .willReturn(aResponse().withStatus(200)))

      Await.result(underTest.issueCallback(callbackUrl, serviceDetails), timeout)

      verify(1, postRequestedFor(urlEqualTo("/publish")))
    }

    "retry the callback url if the first request fails with a 500 response" in new Setup {

      val callbackUrl = s"$wireMockUrl/publish"
      val serviceDetails = ServiceDetails("Hello World", "http://helloworld.example.com", Some(Map("third-party" -> "true")))

      // NOTE: wiremock doesn't do stubbing of multiple requests very elegantly...

      stubFor(post(urlEqualTo("/publish"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo("""{"serviceName":"Hello World","serviceUrl":"http://helloworld.example.com","metadata":{"third-party":"true"},"lastModified":1444043407060}"""))
        .inScenario("retry once")
        .whenScenarioStateIs(Scenario.STARTED)
        .willSetStateTo("RETRY")
        .willReturn(aResponse().withStatus(500)))

      stubFor(post(urlEqualTo("/publish"))
        .withHeader("Content-Type", equalTo("application/json"))
        .inScenario("retry once")
        .whenScenarioStateIs("RETRY")
        .withRequestBody(equalTo("""{"serviceName":"Hello World","serviceUrl":"http://helloworld.example.com","metadata":{"third-party":"true"},"lastModified":1444043407060}"""))
        .willReturn(aResponse().withStatus(200)))

      Await.result(underTest.issueCallback(callbackUrl, serviceDetails), timeout)

      verify(2, postRequestedFor(urlEqualTo("/publish")))
    }

    "not retry the callback url if the first request fails with a 404 response" in new Setup {

      val callbackUrl = s"$wireMockUrl/publish"
      val serviceDetails = ServiceDetails("Hello World", "http://helloworld.example.com", Some(Map("third-party" -> "true")))

      stubFor(post(urlEqualTo("/publish"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo("""{"serviceName":"Hello World","serviceUrl":"http://helloworld.example.com","metadata":{"third-party":"true"},"lastModified":1444043407060}"""))
        .willReturn(aResponse().withStatus(404)))

      try {
        Await.result(underTest.issueCallback(callbackUrl, serviceDetails), timeout)
        fail("Exception was expected but not thrown")
      } catch {
        case _: Throwable => ()
      }

      verify(1, postRequestedFor(urlEqualTo("/publish")))

    }

  }

}
