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

package it.repository

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.models.Subscription
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.repository.SubscriptionRepository

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionRepositorySpec extends UnitSpec
  with MongoSpecSupport with BeforeAndAfterEach with BeforeAndAfterAll {

  private val reactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector: MongoConnector = mongoConnectorForTest
  }

  private val repository = new SubscriptionRepository(reactiveMongoComponent)

  override def beforeEach(): Unit = {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override protected def afterAll(): Unit = {
    await(repository.drop)
  }

  "save" should {

    "create a new Subscription in Mongo and fetch that same Subscription" in {

      val subscription = Subscription("API-Publisher", "http://api-publisher.example.com/publish")
      await(repository.save(subscription))

      val retrieved = await(repository.fetch("API-Publisher")).get

      retrieved shouldBe subscription

    }

    "update an existing Subscription in Mongo and fetch that same Subscription" in {

      val subscription1 = Subscription("API-Publisher", "http://api-publisher.example.com/publish")
      await(repository.save(subscription1))

      val subscription2 = Subscription("API-Publisher", "http://new.api-publisher.example.com/publish")
      await(repository.save(subscription2))

      val retrieved = await(repository.fetch("API-Publisher")).get

      retrieved shouldBe subscription2

    }

  }

  "fetchAll" should {

    "return all Subscriptions in Mongo" in {

      val subscription1 = Subscription("API-Publisher", "http://api-publisher.example.com/publish")
      val subscription2 = Subscription("New-API-Publisher", "http://new.api-publisher.example.com/publish")

      await(repository.save(subscription1))
      await(repository.save(subscription2))

      val retrieved = await(repository.fetchAll())

      retrieved shouldBe Seq(subscription1, subscription2)

    }

  }

}
