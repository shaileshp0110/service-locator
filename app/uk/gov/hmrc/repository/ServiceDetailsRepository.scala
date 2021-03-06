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
import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ServiceDetailsRepository @Inject()(mongo: ReactiveMongoComponent)
  extends ReactiveRepository[ServiceDetails, BSONObjectID]("serviceDetails", mongo.mongoConnector.db,
    ServiceDetails.format, ReactiveMongoFormats.objectIdFormats) {

  def save(serviceDetails: ServiceDetails): Future[ServiceDetails] = {
    collection.find(Json.obj("serviceName"-> serviceDetails.serviceName)).one[BSONDocument].flatMap {
      case Some(document) => collection.update(selector = BSONDocument("_id" -> document.get("_id")), update = serviceDetails)
      case None => collection.insert(serviceDetails)
    }.map(_ => serviceDetails)
  }

  def fetch(serviceName: String): Future[Option[ServiceDetails]] = {
    collection.find(Json.obj("serviceName"-> serviceName)).one[ServiceDetails]
  }

}
