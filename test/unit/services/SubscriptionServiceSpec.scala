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
import uk.gov.hmrc.models.Subscription
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.repository.SubscriptionRepository
import uk.gov.hmrc.services.SubscriptionService

import scala.concurrent.Future

class SubscriptionServiceSpec extends UnitSpec with ScalaFutures with MockitoSugar {

  trait Setup {

    val mockSubscriptionRepository = mock[SubscriptionRepository]

    val underTest = new SubscriptionService(mockSubscriptionRepository)
  }

  "subscribe" should {

    "save the subscription to the repository" in new Setup {

      val subscription = Subscription("API-Publisher", "http://api-publisher.example.com/publish")

      when(mockSubscriptionRepository.save(subscription)).thenReturn(Future.successful(subscription))

      await(underTest.subscribe(subscription))

    }

  }

}
