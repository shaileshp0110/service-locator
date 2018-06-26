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

package it

import com.github.tomakehurst.wiremock.client.WireMock._
import org.joda.time.DateTimeUtils
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import play.api.libs.json.Json
import play.api.test.TestServer
import uk.gov.hmrc.models.JsonFormatters._
import uk.gov.hmrc.models.{ServiceDetails, Subscription}
import uk.gov.hmrc.play.test.WithFakeApplication
import scalaj.http.{Http, HttpResponse}

class ServiceLocatorComponentSpec extends FeatureSpec
  with GivenWhenThen with BeforeAndAfter with WithFakeApplication {

  feature("Service Locator") {

    scenario("Subscriber is notified on Service registration") {

      Given("Subscriber-1 service is subscribed")
      subscribe("Subscriber-1")

      And("Subscriber-2 service is subscribed")
      subscribe("Subscriber-2")

      When("hello-world service is registered")
      register("hello-world")

      Thread.sleep(10000) //Need this as the wiremock calls are made asynchronously

      Then("Subscriber-1 is triggered with hello-world location")
      verifySubscription("hello-world", "Subscriber-1")

      And("Subscriber-2 is triggered with hello-world location")
      verifySubscription("hello-world", "Subscriber-2")

    }

    scenario("Publisher is triggered on subscription") {

      Given("hello-world-1 service is registered")
      register("hello-world-1")

      And("hello-world-2 service is registered")
      register("hello-world-2")

      When("Subscriber service is subscribed")
      subscribe("Subscriber")

      Then("Subscriber is notified with hello-world-1 details")
      verifySubscription("hello-world-1", "Subscriber")

      Then("Subscriber is notified with hello-world-2 details")
      verifySubscription("hello-world-2", "Subscriber")

    }

  }

  before {
    Env.before()
  }

  after {
    Env.after()
  }

  private val server: TestServer = new TestServer(Env.port, fakeApplication)

  override def beforeAll(): Unit = {
    DateTimeUtils.setCurrentMillisFixed(1444046596712L)
    server.start()
  }

  override def afterAll(): Unit = {
    server.stop()
  }

  private def register(serviceName: String): HttpResponse[Array[Byte]] = {
    val data = ServiceDetails(serviceName, s"http://$serviceName.example.com")
    Http(s"${Env.contextUrl}/registration").timeout(5000, 10000)
      .headers(Seq("content-Type" -> "application/json"))
      .postData(Json.toJson(data).toString()).asBytes
  }

  private def subscribe(subscriber: String): HttpResponse[Array[Byte]] = {
    val data = Subscription(subscriber, Env.wiremockUrl + "/notification")
    Http(s"${Env.contextUrl}/subscription").timeout(5000, 10000)
      .headers(Seq("Content-Type" -> "application/json"))
      .postData(Json.toJson(data).toString()).asBytes
  }

  private def verifySubscription(serviceName: String, subscriber: String): Unit = {
    Thread.sleep(1000) // to ensure async callbacks are invoked
    val data = Json.toJson(ServiceDetails(serviceName, s"http://$serviceName.example.com")).toString()
    verify(
      postRequestedFor(urlEqualTo("/notification"))
        .withHeader("Content-Type", containing("application/json"))
        .withRequestBody(equalToJson(data)))
  }

}
