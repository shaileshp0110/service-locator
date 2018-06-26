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

import org.joda.time.{DateTime, DateTimeUtils, DateTimeZone}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.repository.ServiceDetailsRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ServiceDetailsRepositorySpec extends UnitSpec
  with MongoSpecSupport with BeforeAndAfterEach with BeforeAndAfterAll {

  private val reactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector: MongoConnector = mongoConnectorForTest
  }

  private val repository = new ServiceDetailsRepository(reactiveMongoComponent)

  override def beforeEach(): Unit = {
    DateTimeUtils.setCurrentMillisFixed(DateTime.now(DateTimeZone.getDefault).toDate.getTime)
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override protected def afterAll(): Unit = {
    await(repository.drop)
  }

  "createOrUpdate" should {

    "create a new API Definition in Mongo and fetch that same API Definition" in {

      val serviceDetails = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))
      await(repository.save(serviceDetails))

      val retrieved = await(repository.fetch("Hello World")).get

      retrieved shouldBe serviceDetails

    }

    "update an existing API Definition in Mongo and fetch that same API Definition" in {

      val serviceDetails1 = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true")))
      await(repository.save(serviceDetails1))

      val serviceDetails2 = ServiceDetails("Hello World", "http://hello-world.example.com", Some(Map("third-party" -> "true", "version" -> "1.0.1")))
      await(repository.save(serviceDetails2))

      val retrieved = await(repository.fetch("Hello World")).get

      retrieved shouldBe serviceDetails2

    }

  }

}
