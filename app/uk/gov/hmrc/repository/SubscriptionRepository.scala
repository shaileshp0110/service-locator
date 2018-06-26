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

package uk.gov.hmrc.repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson._
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.models.Subscription
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubscriptionRepository @Inject()(mongo: ReactiveMongoComponent)
  extends ReactiveRepository[Subscription, BSONObjectID]("subscriptions", mongo.mongoConnector.db,
    Subscription.format, ReactiveMongoFormats.objectIdFormats) {

  def save(subscription: Subscription): Future[Subscription] = {
    collection.find(Json.obj("serviceName" -> subscription.serviceName)).one[BSONDocument].flatMap {
      case Some(document) =>
        collection.update(selector = BSONDocument("_id" -> document.get("_id")), update = subscription)
      case None => collection.insert(subscription)
    }.map(_ => subscription)
  }

  def fetch(serviceName: String): Future[Option[Subscription]] = {
    collection.find(Json.obj("serviceName" -> serviceName)).one[Subscription]
  }

  def fetchAll(): Future[Seq[Subscription]] = {
    collection.find(Json.obj()).cursor[Subscription].collect[Seq]()
  }
}