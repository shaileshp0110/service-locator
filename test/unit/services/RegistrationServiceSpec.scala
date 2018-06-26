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

package unit.services

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.test.{FakeApplication, Helpers}
import uk.gov.hmrc.config.AppContext
import uk.gov.hmrc.connectors.SubscriptionCallbackConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.models.{ServiceDetails, Subscription}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.repository.{ServiceDetailsRepository, SubscriptionRepository}
import uk.gov.hmrc.services.RegistrationService

import scala.concurrent.Future

class RegistrationServiceSpec extends UnitSpec with ScalaFutures with MockitoSugar {

  private trait Setup {

    val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))

    implicit val hc = HeaderCarrier()

    val mockServiceDetailsRepository = mock[ServiceDetailsRepository]
    val mockSubscriptionRepository = mock[SubscriptionRepository]
    val mockSubscriptionCallbackConnector = mock[SubscriptionCallbackConnector]
    val mockAppContext = mock[AppContext]

    when(mockAppContext.subscriptionDelayInSeconds).thenReturn(0)

    val underTest = new RegistrationService(mockServiceDetailsRepository, mockSubscriptionRepository, mockSubscriptionCallbackConnector, mockAppContext)
  }

  "register" should {

    "register the given service detail and invoke all subscribed callbacks" in new Setup {

      Helpers.running(FakeApplication()) {
        val subscription1 = Subscription("API-Publisher", "http://api-publisher.example.com/publish")
        val subscription2 = Subscription("Another-API-Publisher", "http://another.api-publisher.example.com/publish")

        when(mockServiceDetailsRepository.save(serviceDetails)).thenReturn(Future.successful(serviceDetails))
        when(mockSubscriptionRepository.fetchAll()).thenReturn(Future.successful(Seq(subscription1, subscription2)))

        when(mockSubscriptionCallbackConnector.issueCallback("http://api-publisher.example.com/publish", serviceDetails)).thenReturn(Future.successful(()))
        when(mockSubscriptionCallbackConnector.issueCallback("http://another.api-publisher.example.com/publish", serviceDetails)).thenReturn(Future.successful(()))

        await(underTest.register(serviceDetails))
        Thread.sleep(1000) //Need this as the wiremock calls are made asynchronously

        verify(mockSubscriptionCallbackConnector).issueCallback("http://api-publisher.example.com/publish", serviceDetails)
        verify(mockSubscriptionCallbackConnector).issueCallback("http://another.api-publisher.example.com/publish", serviceDetails)
      }
    }

  }

  "fetchService" should {

    "return the expected service" in new Setup {
      when(mockServiceDetailsRepository.fetch(serviceDetails.serviceName)).thenReturn(Future.successful(Some(serviceDetails)))

      val result = await(underTest.fetchService(serviceDetails.serviceName))

      result shouldBe Some(serviceDetails)
    }

  }

}
